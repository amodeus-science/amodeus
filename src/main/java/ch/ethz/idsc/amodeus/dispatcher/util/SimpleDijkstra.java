/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.FastDijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.PreProcessDijkstra;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public enum SimpleDijkstra {
    ;

    public static LeastCostPathCalculator prepDijkstra(Network network) {
        PreProcessDijkstra preProcessData = new PreProcessDijkstra();
        preProcessData.run(network);

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

        // return new FastDijkstraFactory(preProcessData).createPathCalculator(network, travelMinCost, travelTime);
        return new FastDijkstraFactory(true).createPathCalculator(network, travelMinCost, travelTime);
    }

    public static LeastCostPathCalculator.Path executeDijkstra(LeastCostPathCalculator dijkstra, Node from, Node to) {
        // depending on implementation of traveldisutility and traveltime, starttime, person and vehicle are needed
        return dijkstra.calcLeastCostPath( //
                Objects.requireNonNull(from), //
                Objects.requireNonNull(to), //
                0.0, null, null);
    }

}
