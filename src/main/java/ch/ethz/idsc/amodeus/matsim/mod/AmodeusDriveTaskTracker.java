/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.path.DivertedVrpPath;
import org.matsim.contrib.dvrp.path.VrpPath;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.OnlineTrackerListener;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.contrib.dvrp.vrpagent.VrpLeg;
import org.matsim.core.mobsim.framework.MobsimTimer;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class AmodeusDriveTaskTracker implements OnlineDriveTaskTracker {
    public static final boolean DEBUG = false;
    // ---
    private final DvrpVehicle vehicle;
    private final DriveTask driveTask;
    private final VrpLeg vrpDynLeg;

    private final OnlineTrackerListener optimizer;
    private final MobsimTimer timer;

    private VrpPath path;
    private int currentLinkIdx;
    private double linkEnterTime;
    private double[] remainingTTs;// excluding the current link

    AmodeusDriveTaskTracker(DvrpVehicle vehicle, VrpLeg vrpDynLeg, OnlineTrackerListener optimizer, MobsimTimer timer) {
        this.vehicle = vehicle;
        this.driveTask = (DriveTask) vehicle.getSchedule().getCurrentTask();
        this.vrpDynLeg = vrpDynLeg;
        this.optimizer = optimizer;
        this.timer = timer;

        initForPath(driveTask.getPath());
        currentLinkIdx = 0;
        linkEnterTime = driveTask.getBeginTime();
    }

    private void initForPath(VrpPath path) {
        this.path = path;
        remainingTTs = new double[path.getLinkCount()];

        double tt = 0;
        for (int i = remainingTTs.length - 1; i >= 0; i--) {
            remainingTTs[i] = tt;
            tt += path.getLinkTravelTime(i);
        }
    }

    @Override
    public void movedOverNode(Link nextLink) {
        currentLinkIdx++;
        linkEnterTime = timer.getTimeOfDay();
        optimizer.vehicleEnteredNextLink(vehicle, nextLink);
    }

    /** Assumption: vehicle is diverted as soon as possible, i.e.:
     * <ul>
     * <li>if the next link can be changed: after the current link</li>
     * <li>If not then, (a) if the current link is not the last one, after the
     * next link, or</li>
     * <li>(b) no diversion possible (the leg ends on the current link)</li>
     * </ul>
    */
    @Override
    public LinkTimePair getDiversionPoint() {
        if (vrpDynLeg.canChangeNextLink())
            return new LinkTimePair(path.getLink(currentLinkIdx), predictLinkExitTime());

        // the current link is the last one
        if (path.getLinkCount() == currentLinkIdx + 1)
            // too late to divert (reason: cannot change the next link)
            return null;

        double nextLinkTT = path.getLinkTravelTime(currentLinkIdx + 1);
        double predictedNextLinkExitTime = predictLinkExitTime() + nextLinkTT;
        return new LinkTimePair(path.getLink(currentLinkIdx + 1), predictedNextLinkExitTime);
    }

    /** @return {@link LinkTimePair} on which the {@link RoboTaxi} can be
     *         diverted, i.e., its patch can be changed at this link. */
    public LinkTimePair getSafeDiversionPoint() {
        return (Objects.nonNull(getDiversionPoint())) ? //
                getDiversionPoint() : //
                getPathEndDiversionPoint();
    }

    /** @return {@link LinkTimePair} at which the {@link RoboTaxi} ends its
     *         current path as a backup diversion point. */
    private LinkTimePair getPathEndDiversionPoint() {
        System.err.println("Diversionpoint was null, returning path end point as diversion point.");
        LinkTimePair returnPair = new LinkTimePair(path.getToLink(), predictLinkExitTime());
        GlobalAssert.that(Objects.nonNull(returnPair));
        return returnPair;
    }

    @Override
    public void divertPath(VrpPathWithTravelData newSubPath) {
        LinkTimePair diversionPoint = getSafeDiversionPoint();
        GlobalAssert.that(Objects.nonNull(newSubPath));
        GlobalAssert.that(Objects.nonNull(diversionPoint));
        GlobalAssert.that(Objects.nonNull(newSubPath.getFromLink()));
        GlobalAssert.that(Objects.nonNull(diversionPoint.link));

        if (!newSubPath.getFromLink().equals(diversionPoint.link)) {
            throw new IllegalArgumentException("links dont match: " + newSubPath.getFromLink().getId() + "!=" + diversionPoint.link.getId());
        }
        if (newSubPath.getDepartureTime() != diversionPoint.time) {
            throw new IllegalArgumentException("times dont match" + newSubPath.getDepartureTime() + "!=" + diversionPoint.time);
        }

        int diversionLinkIdx = getDiversionLinkIndex();

        DivertedVrpPath divertedPath = new DivertedVrpPath(path, newSubPath, diversionLinkIdx);

        initForPath(divertedPath);

        vrpDynLeg.pathDiverted(divertedPath);
        driveTask.pathDiverted(divertedPath, newSubPath.getArrivalTime());

    }

    @Override
    public double predictEndTime() {
        return predictLinkExitTime() + remainingTTs[currentLinkIdx];
    }

    private double predictLinkExitTime() {
        return Math.max(timer.getTimeOfDay(), linkEnterTime + path.getLinkTravelTime(currentLinkIdx));
    }

    @Override
    public int getCurrentLinkIdx() {
        return currentLinkIdx;
    }

    public int getDiversionLinkIndex() {
        return getCurrentLinkIdx() + (vrpDynLeg.canChangeNextLink() ? 0 : 1);
    }

    @Override
    public VrpPath getPath() {
        return path;
    }
}
