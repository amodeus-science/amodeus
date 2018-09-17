/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.util.math.Stopwatch;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public class BipartiteMatchingUtils {

    /** network distance function used to prevent cycling solutions */
    private final DistanceFunction accDstFctn;
    private final Stopwatch stopwatch1 = Stopwatch.stopped();
    private final Stopwatch stopwatch2 = Stopwatch.stopped();

    public BipartiteMatchingUtils(Network network) {
        accDstFctn = new NetworkDistanceFunction(network, new FastAStarLandmarksFactory());
    }

    public Tensor executePickup( //
            UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network, boolean reduceWkdTree) {
        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, AVRequest> gbpMatch;

        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */

        stopwatch1.start();
        GlobalBipartiteMatching globalBipartiteMatching = new GlobalBipartiteMatching(distanceFunction);
        if (reduceWkdTree) {
            KdTreeReducer reducer = new KdTreeReducer(roboTaxis, requests, distanceFunction, network, infoLine);
            gbpMatch = globalBipartiteMatching.match(reducer.getReducedRoboTaxis(), reducer.getReducedRequests());
        } else {
            gbpMatch = globalBipartiteMatching.match(roboTaxis, requests);
        }
        stopwatch1.stop();
        System.out.println("stopwatch1 :  " +  stopwatch1.display_nanoSeconds()/1000000);

        stopwatch2.start();
        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        Map<RoboTaxi, AVRequest> gbpMatchCleaned = CyclicSolutionPreventer.apply(gbpMatch, universalDispatcher, accDstFctn);
        stopwatch2.stop();
        System.out.println("stopwatch2 :  " +  stopwatch2.display_nanoSeconds()/1000000);

        /** perform dispatching */
        for (Entry<RoboTaxi, AVRequest> entry : gbpMatchCleaned.entrySet())
            universalDispatcher.setRoboTaxiPickup(entry.getKey(), entry.getValue());

        // infoLine.append(Tensors.vector(stopwatch1.display_seconds(), stopwatch2.display_seconds()).map(Round._3));
        return infoLine;
    }

    public Map<RoboTaxi, AVRequest> getGBPMatch( //
            UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network, boolean reduceWkdTree) {

        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, AVRequest> gbpMatch;

        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        if (reduceWkdTree) {
            KdTreeReducer reducer = new KdTreeReducer(roboTaxis, requests, distanceFunction, network, infoLine);
            gbpMatch = ((new GlobalBipartiteMatching(distanceFunction)).match(reducer.getReducedRoboTaxis(), reducer.getReducedRequests()));
        } else {
            gbpMatch = ((new GlobalBipartiteMatching(distanceFunction)).match(roboTaxis, requests));
        }

        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        Map<RoboTaxi, AVRequest> gbpMatchCleaned = CyclicSolutionPreventer.apply(gbpMatch, universalDispatcher, accDstFctn);

        return gbpMatchCleaned;
    }

}
