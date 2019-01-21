package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum EverySubtripIsValid {
    ;
    /** this function check the if every sub-trip of a trip is valid trip ]
     * 
     * @param listOfTripsFromLastLoop
     * @param thisTrip
     * @return */
    static boolean of(List<Set<AVRequest>> listOfTripsFromLastLoop, Set<AVRequest> thisTrip) {
        // generate all sub-trips
        List<Set<AVRequest>> listOfSubtrips = new ArrayList<>();
        List<AVRequest> listOfSingleRequest = new ArrayList<>();
        listOfSingleRequest.addAll(thisTrip);
        // for (AVRequest avRequest : thisTrip) {
        // listOfSingleRequest.add(avRequest);
        // }
        for (int i = 0; i < listOfSingleRequest.size(); i++) {
            Set<AVRequest> subtrip = new HashSet<>();
            subtrip.addAll(listOfSingleRequest.subList(0, i));
            // for (int j = 0; j < i; j++) {
            // subtrip.add(listOfSingleRequest.get(j));
            // }
            subtrip.addAll(listOfSingleRequest.subList(i + 1, listOfSingleRequest.size()));
            // for (int j = i + 1; j < listOfSingleRequest.size(); j++) {
            // subtrip.add(listOfSingleRequest.get(j));
            // }
            listOfSubtrips.add(subtrip);
        }

        for (Set<AVRequest> thisSubTrip : listOfSubtrips) {
            if (!listOfTripsFromLastLoop.contains(thisSubTrip)) {
                return false;
            }
        }

        return true;
    }
}
