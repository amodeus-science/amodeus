/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Set;

public enum RStatusHelper {
    ;

    public static boolean unserviced(Set<RequestStatus> set) {
        boolean inService = set.contains(RequestStatus.PICKUP) //
                || set.contains(RequestStatus.DRIVING) //
                || set.contains(RequestStatus.DROPOFF);
        return !inService;
    }
}
