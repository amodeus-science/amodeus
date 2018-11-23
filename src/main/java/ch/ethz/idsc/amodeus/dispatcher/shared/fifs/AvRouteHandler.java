/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/*package*/ class AvRouteHandler {

    NavigableMap<Double, Map<RoboTaxi, Set<SharedAvRoute>>> routes = new TreeMap<>();

    /* package */ void add(RoboTaxi roboTaxi, SharedAvRoute sharedAvRoute) {
        Double endTime = sharedAvRoute.getEndTime();
        if (!routes.containsKey(endTime)) {
            routes.put(endTime, new HashMap<>());
        }
        if (!routes.get(endTime).containsKey(roboTaxi)) {
            routes.get(endTime).put(roboTaxi, new HashSet<>());
        }
        routes.get(endTime).get(roboTaxi).add(sharedAvRoute);
    }

    /* package */ void remove(Double doubleValue) {
        routes.remove(doubleValue);
    }

    /* package */ boolean contains(Double containingValue) {
        return routes.containsKey(containingValue);
    }

    /* package */ Map<RoboTaxi, Set<SharedAvRoute>> getCopyOfNext() {
        return new HashMap<>(routes.firstEntry().getValue());
    }

    /* package */ int getNumbervalues() {
        return routes.size();
    }

    /* package */ double getNextvalue() {
        return routes.firstKey();
    }

    /* package */ boolean isEmpty() {
        return routes.isEmpty();
    }

}
