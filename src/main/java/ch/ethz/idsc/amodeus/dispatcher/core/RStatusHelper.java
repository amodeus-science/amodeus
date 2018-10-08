package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Set;

public class RStatusHelper {

    public static boolean unserviced(Set<RequestStatus> statii) {
        boolean inService = statii.contains(RequestStatus.PICKUP) || //
                statii.contains(RequestStatus.DRIVING) || //
                statii.contains(RequestStatus.DROPOFF);
        return !inService;
    }
}
