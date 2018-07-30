package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum GlobalBipartiteMatching {
    ;

    public static Map<RoboTaxi, AVRequest> of(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunction) {
        return ((new HungarBiPartVehicleDestMatcher(//
                distanceFunction)).matchAVRequest(roboTaxis, requests));
    }

}
