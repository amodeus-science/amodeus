/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

/* package */ enum EverySubtripIsValid {
    ;
    /** this function check the if every sub-trip of a trip is valid trip ]
     * 
     * @param listOfTripsFromLastLoop
     * @param thisTrip
     * @return */
    public static boolean of(List<Set<PassengerRequest>> listOfTripsFromLastLoop, Set<PassengerRequest> thisTrip) {
        List<PassengerRequest> listOfSingleRequest = new ArrayList<>(thisTrip);

        // generate all sub-trips
        List<Set<PassengerRequest>> listOfSubtrips = listOfSingleRequest.stream().map(avRequest -> {
            HashSet<PassengerRequest> set = new HashSet<>(listOfSingleRequest);
            set.remove(avRequest);
            return set;
        }).collect(Collectors.toList());

        return listOfTripsFromLastLoop.containsAll(listOfSubtrips);
    }
}
