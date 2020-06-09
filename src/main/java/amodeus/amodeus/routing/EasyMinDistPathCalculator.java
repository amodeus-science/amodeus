/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public enum EasyMinDistPathCalculator {
    ;

    /** Method can be used to rapidly create a {@link LeastCostPathCalculator}
     * in the @param network based on the @param calcFactory such that the path minimizes
     * the network distance, @return a {@link LeastCostPathCalculator} to be used in
     * subsequent steps. Example usage:
     * 
     * EasyMinDistPathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory()) */
    public static LeastCostPathCalculator prepPathCalculator(Network network, //
            LeastCostPathCalculatorFactory calcFactory) {
        TravelDisutility travelMinCost = new TravelDisutility() {
            @Override
            public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
                return getLinkMinimumTravelDisutility(link);
            }

            @Override
            public double getLinkMinimumTravelDisutility(Link link) {
                return link.getLength();
            }
        };
        TravelTime travelTime = new TravelTime() {
            @Override
            public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
                return link.getLength() / link.getFreespeed();
            }
        };
        return calcFactory.createPathCalculator(network, travelMinCost, travelTime);
    }
}
