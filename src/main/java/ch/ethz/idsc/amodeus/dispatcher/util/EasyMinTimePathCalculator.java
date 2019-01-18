/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum EasyMinTimePathCalculator {
    ;

    public static LeastCostPathCalculator prepPathCalculator(Network network, LeastCostPathCalculatorFactory calcFactory) {
        TravelDisutility travelMinCost = new TravelDisutility() {
            @Override
            public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
                return getLinkMinimumTravelDisutility(link);
            }

            @Override
            public double getLinkMinimumTravelDisutility(Link link) {
                return link.getLength() / link.getFreespeed();
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

    public static LeastCostPathCalculator.Path execPathCalculator(LeastCostPathCalculator pathCalc, Node from, Node to) {
        // depending on implementation of traveldisutility and traveltime, starttime,
        // person and vehicle are needed
        GlobalAssert.that(pathCalc != null);
        GlobalAssert.that(from != null);
        GlobalAssert.that(to != null);
        return pathCalc.calcLeastCostPath(from, to, 0.0, null, null);
    }
}
