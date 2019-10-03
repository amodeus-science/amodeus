/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.linkspeed.LinkIndex;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedTimeSeries;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.tensor.Scalar;

/** This class is used to complete modified link speeds in a network at a certain time
 * when no recording was made at that time. The principal idea is that a kernel
 * is used to identify neighbors and then the change of the speed of that link is the
 * same as the average change of its neighbors.
 * 
 * @author clruch */
public class LinkSpeedDataInterpolation {

    private final NeighborKernel kernel;
    private final Network network;
    private final LinkSpeedDataContainer lsData;
    private final MatsimAmodeusDatabase db;

    public LinkSpeedDataInterpolation(Network network, NeighborKernel filterKernel, //
            LinkSpeedDataContainer lsData, MatsimAmodeusDatabase db) {
        this.network = network;
        this.kernel = filterKernel;
        this.lsData = lsData;
        this.db = db;
        completeSpeeds();
    }

    private void completeSpeeds() {
        HashSet<Link> interpolatedLinks = new HashSet<>();

        /** identify all times for which the {@link LinkSpeedDataContainer} contains recordings */
        Set<Integer> recordedTimes = lsData.getRecordedTimes();

        /** for every link in the network, complete recordings based on local average
         * if not present. */
        int completed = 0;
        for (Link link : network.getLinks().values()) {

            ++completed;
            if (completed % 10000 == 0)
                System.out.println(completed + " / " + network.getLinks().size());
            Objects.requireNonNull(link);
            String linkID = LinkIndex.fromLink(link);

            // /** same blocking for all links */
            // if (!lsData.getLinkSet().containsKey(db.getLinkIndex(link))) {
            // for (Integer time : recordedTimes) {
            // lsData.addData(linkID, time, 0.0001);
            // }
            // }

            /** some recordings exist for link */
            if (lsData.getLinkSet().containsKey(linkID)) {
                LinkSpeedTimeSeries timeSeries = lsData.getLinkSet().get(linkID);
                for (Integer time : recordedTimes) {
                    if (!timeSeries.getRecordedTimes().contains(time)) {
                        System.out.println("Are we ever here?... ");
                        System.exit(1);
                        Scalar speed = MeanLinkSpeed.ofNeighbors(kernel.getNeighbors(link), time, link, db, lsData);
                        if (Objects.nonNull(speed)) {
                            lsData.addData(linkID, time, speed.number().doubleValue());
                            interpolatedLinks.add(link);
                        }

                    }
                }
            } else { /** no recordings exist for link */
                for (Integer time : recordedTimes) {
                    Scalar speed = MeanLinkSpeed.ofNeighbors(kernel.getNeighbors(link), time, link, db, lsData);
                    if (Objects.nonNull(speed)) {
                        lsData.addData(linkID, time, speed.number().doubleValue());
                        interpolatedLinks.add(link);
                    }
                }
            }
        }
        System.err.println("Total number of unsuccessful link speed estimations: " //
                + interpolatedLinks.size() + "(" + interpolatedLinks.size() / ((double) network.getLinks().size()) + ")");
    }
}
