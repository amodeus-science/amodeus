/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum EverySubtripIsValid {
    ;
    /** this function check the if every sub-trip of a trip is valid trip ]
     * 
     * @param listOfTripsFromLastLoop
     * @param thisTrip
     * @return */
    public static boolean of(List<Set<AVRequest>> listOfTripsFromLastLoop, Set<AVRequest> thisTrip) {
        List<AVRequest> listOfSingleRequest = new ArrayList<>(thisTrip);

        // generate all sub-trips
        List<Set<AVRequest>> listOfSubtrips = listOfSingleRequest.stream().map(avRequest -> {
            HashSet<AVRequest> set = new HashSet<>(listOfSingleRequest);
            set.remove(avRequest);
            return set;
        }).collect(Collectors.toList());

        return listOfTripsFromLastLoop.containsAll(listOfSubtrips);
    }
}
