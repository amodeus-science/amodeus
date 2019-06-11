package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.matsim.av.passenger.AVRequest;

/** @author Lu Chengqi This is the space time global bipartite matching module. This is currently for unit
 *         capacity dispatcher only. To use this, special dispatcher is recommended.
 *         First, all the vehicles (including the passenger carrying ones) should be input to the
 *         vehicle side. Second, before doing the matching, remember to update current time "now",
 *         otherwise it will not function properly! */
public class GlobalBipartiteSpaceTimeMatching extends AbstractRoboTaxiDestMatcher {
    // fields
    private static final double AVERAGE_SPEED = 20.0;
    private static final double EUC_TO_NET_RATIO = 1.414; // euclidean distance to network distance ratio
    private double now = 0.0;
    protected final GlobalBipartiteWeight specificWeight;

    // constructor
    public GlobalBipartiteSpaceTimeMatching(DistanceFunction distanceFunction) {
        this.specificWeight = new GlobalBipartiteWeight() {
            @Override
            public double between(RoboTaxi roboTaxi, Link link) {
                if (roboTaxi.isDivertable())
                    return distanceFunction.getDistance(roboTaxi, link);
                // for passenger carrying vehicles, we take the location and time the car is expecting
                // to finish dropping off passenger and become available
                Link divertableLink = roboTaxi.getCurrentDriveDestination();
                double divertableTime = Schedules.getLastTask(roboTaxi.getSchedule()).getBeginTime();
                double distanceConvertedFromTime = (divertableTime - now) * AVERAGE_SPEED / EUC_TO_NET_RATIO;
                if (divertableTime - now == -1)
                    distanceConvertedFromTime = 0;

                // DELETE the part below after debugging
                if (divertableTime - now < -1) {
                    System.err.println(roboTaxi.getStatus().toString());
                    System.err.println(roboTaxi.getSchedule().getCurrentTask().toString());
                    System.err.println(Schedules.getLastTask(roboTaxi.getSchedule()).toString());
                    System.err.println(divertableTime - now);
                    System.err.println("ATTENTION!!! Something is wrong!!!");
                    distanceConvertedFromTime = 9999999.9;
                }
                // Until here

                double augmentedDistance = //
                        distanceFunction.getDistance(divertableLink, link) + distanceConvertedFromTime;
                return augmentedDistance;
            }
        };
    }

    // methods
    @Override
    protected Map<RoboTaxi, AVRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> avRequests) {
        return GlobalBipartiteHelper.genericMatch(roboTaxis, avRequests, AVRequest::getFromLink, specificWeight);
    }

    @Override
    protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
        return GlobalBipartiteHelper.genericMatch(roboTaxis, links, link -> link, specificWeight);
    }

    // IMPORTANT!!! This function has to be implemented in the dispatcher, otherwise it won't work properly!!!
    @Override
    protected void updateCurrentTime(double now) {
        this.now = now;
    }

}
