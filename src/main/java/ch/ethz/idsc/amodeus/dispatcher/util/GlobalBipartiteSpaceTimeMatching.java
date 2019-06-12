/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Task;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.matsim.av.passenger.AVRequest;

/** This is the space time global bipartite matching module. This is currently for unit
 * capacity dispatcher only. To use this, special dispatcher is recommended.
 * First, all the vehicles (including the passenger carrying ones) should be input to the
 * vehicle side. Second, before doing the matching, remember to update current time "now",
 * otherwise it will not function properly!
 * 
 * @author Lu Chengqi */
public class GlobalBipartiteSpaceTimeMatching extends AbstractRoboTaxiDestMatcher {
    // fields
    private static final double AVERAGE_SPEED = 10.0;
    private static final double EUC_TO_NET_RATIO = 1.414; // euclidean distance to network distance ratio
    double now;
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
                List<? extends Task> listOfSchedules = roboTaxi.getSchedule().getTasks();
                Task lastTask = listOfSchedules.get(listOfSchedules.size() - 1); // this will be the stay task
                double divertableTime = lastTask.getBeginTime();
                double distanceConvertedFromTime = (divertableTime - now) * AVERAGE_SPEED / EUC_TO_NET_RATIO;
                if (distanceConvertedFromTime < 0) {
                    System.err.println("ATTENTION!!! Something is wrong!!!");
                    distanceConvertedFromTime = 0;
                }
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

}
