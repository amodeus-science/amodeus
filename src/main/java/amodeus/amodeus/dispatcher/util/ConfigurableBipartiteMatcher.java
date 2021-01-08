/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.UniversalDispatcher;
import amodeus.amodeus.routing.DistanceFunction;
import amodeus.amodeus.util.matsim.SafeConfig;

public class ConfigurableBipartiteMatcher extends BipartiteMatcherInternal {

    private final AbstractRoboTaxiDestMatcher globalBipartiteMatcher;
    private final Boolean hungarian;

    /** Allows to instantiate a configurable bipartite matching algorithm via the av.xml file, there are two options:
     * - 1 (default option): the Hungarian method is used, this is chosen if no specification is given in av.xml or the specification
     * 
     * <dispatcher strategy="GlobalBipartiteMatchingDispatcher">
     * <param name="matchingAlgorithm" value="HUNGARIAN" />
     * 
     * -2: solution of the assignment problem via Integer Linear Program, for this option the av.xml file should look as follows
     * <dispatcher strategy="GlobalBipartiteMatchingDispatcher">
     * <param name="matchingAlgorithm" value="ILP" />
     * <param name="matchingWeight" value="[1.0,1.0,1.0]" />
     * 
     * The values are retrieved via @param safeConfig, other parameters necessary for instantiation are
     * the network @param network, and the distance function @param distanceFunction */
    public ConfigurableBipartiteMatcher(Network network, GlobalBipartiteCost cost, SafeConfig safeConfig) {
        super(network);
        String matchingAlg = safeConfig.getString("matchingAlgorithm", "HUNGARIAN");
        switch (matchingAlg) {
        case "HUNGARIAN":
            hungarian = true;
            globalBipartiteMatcher = new GlobalBipartiteMatching(cost);
            break;
        case "ILP":
            hungarian = false;
            globalBipartiteMatcher = new GlobalBipartiteMatchingILP(cost, safeConfig);
            break;
        default:
            // hungarian = null;
            // globalBipartiteMatcher = null;
            throw new RuntimeException("An invalid option for the matching algorithm was chosen. " + matchingAlg);
        }
    }

    @Override
    public Map<RoboTaxi, PassengerRequest> getGBPMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network) {
        if (hungarian)
            return hungarianMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
        return integerLinearProgramMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
    }

    private Map<RoboTaxi, PassengerRequest> hungarianMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network) {
        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        Map<RoboTaxi, PassengerRequest> gbpMatch = globalBipartiteMatcher.match(roboTaxis, requests);
        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        return CyclicSolutionPreventer.apply(gbpMatch, universalDispatcher, accDstFctn);
    }

    private Map<RoboTaxi, PassengerRequest> integerLinearProgramMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network) {
        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        return globalBipartiteMatcher.match(roboTaxis, requests);
        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
    }

}
