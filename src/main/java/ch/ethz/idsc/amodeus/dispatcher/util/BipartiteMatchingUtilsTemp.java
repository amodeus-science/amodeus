/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum BipartiteMatchingUtilsTemp {
    ;

    public static Tensor executePickup(BiConsumer<RoboTaxi, AVRequest> setFunction, Map<AVRequest, RoboTaxi> currentPickups,Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunctionAccurate, Network network, boolean reducewithKDTree) {
        Tensor infoLine = Tensors.empty();
        DistanceFunction distanceFunctionFast = new EuclideanDistanceFunction();
        Map<RoboTaxi, AVRequest> gbpMatch = globalBipartiteMatching(roboTaxis, requests, distanceFunctionFast, network, infoLine, reducewithKDTree);
        Map<RoboTaxi,AVRequest> gbpMatchCheck = checkMatch(gbpMatch, distanceFunctionAccurate, currentPickups);                
        for (Entry<RoboTaxi, AVRequest> entry : gbpMatchCheck.entrySet()) {
            setFunction.accept(entry.getKey(), entry.getValue());
        }
        return infoLine;
    }

    public static Tensor executeRebalance(BiConsumer<RoboTaxi, Link> setFunction, Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunction, Network network, boolean reducewithKDTree) {
        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, AVRequest> gbpMatch = globalBipartiteMatching(roboTaxis, requests, distanceFunction, network, infoLine, reducewithKDTree);
        for (Entry<RoboTaxi, AVRequest> entry : gbpMatch.entrySet()) {
            setFunction.accept(entry.getKey(), entry.getValue().getFromLink());
        }
        return infoLine;
    }

    private static Map<RoboTaxi, AVRequest> globalBipartiteMatching(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunction, Network network, Tensor infoLine, boolean reducewithKDTree) {

        if (reducewithKDTree == true && !(distanceFunction instanceof EuclideanDistanceFunction)) {
            System.err.println("cannot use requestReducing technique with other distance function than Euclidean.");
            GlobalAssert.that(false);
        }

        // save initial problemsize
        infoLine.append(Tensors.vectorInt(roboTaxis.size(), requests.size()));

        if (reducewithKDTree) {
            // 1) In case roboTaxis >> requests reduce search space using kd-trees
            Collection<RoboTaxi> roboTaxisReduced = StaticHelper.reduceRoboTaxis(requests, roboTaxis, network);

            // 2) In case requests >> roboTaxis reduce the search space using kd-trees
            Collection<AVRequest> requestsReduced = StaticHelper.reduceRequests(requests, roboTaxis, network);

            // 3) compute Euclidean bipartite matching for all vehicles using the Hungarian method and set new pickup commands
            infoLine.append(Tensors.vectorInt(roboTaxisReduced.size(), requestsReduced.size()));

            return ((new HungarBiPartVehicleDestMatcher(//
                    distanceFunction)).matchAVRequest(roboTaxisReduced, requestsReduced));
        }
        return ((new HungarBiPartVehicleDestMatcher(//
                distanceFunction)).matchAVRequest(roboTaxis, requests));
    }
    
    
    private static Map<RoboTaxi,AVRequest> checkMatch(Map<RoboTaxi,AVRequest> matchUnchckd, // 
            DistanceFunction distanceFunctionAcc, Map<AVRequest, RoboTaxi> currentPickups){
        Map<RoboTaxi,AVRequest> chckdMap = new HashMap<>();
        
        for(Entry<RoboTaxi, AVRequest> entry : matchUnchckd.entrySet()){
            RoboTaxi newTaxi = entry.getKey();
            RoboTaxi asgndTaxi = currentPickups.get(entry.getValue());
            if(Objects.isNull(asgndTaxi)){
                chckdMap.put(newTaxi, entry.getValue());
            }else{
                double distNew = distanceFunctionAcc.getDistance(newTaxi, entry.getValue()) ;
                double distAss = distanceFunctionAcc.getDistance(asgndTaxi, entry.getValue());
                if(distNew < distAss*0.99){
                    chckdMap.put(newTaxi, entry.getValue());
                }
            }
            
        }
        
        return chckdMap;
        
    }
    
}
