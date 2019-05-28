/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class ConfigurableBipartiteMatcher extends BipartiteMatchingUtilsSuper {

    private final GlobalBipartiteMatching2 tempGlMatch;
    private final Boolean hungarian;

    public ConfigurableBipartiteMatcher(Network network, DistanceFunction distanceFunction, SafeConfig safeConfig) {
        super(network);
        tempGlMatch = new GlobalBipartiteMatching2(distanceFunction);

        String matchingAlg = safeConfig.getString("matchingAlgorithm", "HUNGARIAN");

        if (matchingAlg.equals("HUNGARIAN")) {
            hungarian = true;
        } else if (matchingAlg.equals("ILP")) {
            hungarian = false;
        } else {
            System.err.println("An invalid option for the matching algorithm was chosen.");
            hungarian = null;
            GlobalAssert.that(false);
        }

    }

    public Map<RoboTaxi, AVRequest> getGBPMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {

        if (hungarian)
            return hungarianMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
        else
            return integerLinearProgramMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);

    }

    private Map<RoboTaxi, AVRequest> hungarianMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {
        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        Map<RoboTaxi, AVRequest> gbpMatch = ((new GlobalBipartiteMatching(distanceFunction)).match(roboTaxis, requests));
        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        Map<RoboTaxi, AVRequest> gbpMatchCleaned = CyclicSolutionPreventer.apply(gbpMatch, universalDispatcher, accDstFctn);
        return gbpMatchCleaned;
    }

    private Map<RoboTaxi, AVRequest> integerLinearProgramMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {

        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        Map<RoboTaxi, AVRequest> gbpMatch = (tempGlMatch.match(roboTaxis, requests));

        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        Map<RoboTaxi, AVRequest> gbpMatchCleaned = CyclicSolutionPreventer.apply(gbpMatch, universalDispatcher, accDstFctn);

        return gbpMatchCleaned;

    }

}

// @Override
// protected Tensor executeGeneralPickup(UniversalDispatcher universalDispatcher, //
// Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
// Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
// DistanceFunction distanceFunction, CachedNetworkTimeDistance distanceCashed, Network network) {
// Tensor infoLine = Tensors.empty();
// Map<RoboTaxi, AVRequest> gbpMatchCleaned = getGBPMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
// /** perform dispatching */
// for (Entry<RoboTaxi, AVRequest> entry : gbpMatchCleaned.entrySet())
// universalDispatcher.setRoboTaxiPickup(entry.getKey(), entry.getValue());
// return infoLine;
// }