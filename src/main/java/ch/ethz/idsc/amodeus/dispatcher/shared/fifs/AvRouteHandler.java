/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/** Stores {@link SharedAvRoute}s in the order of the end Time. Like that it is easy to keep track of the fastest routes. Furthermore It keeps track of the
 * routes for different RoboTaxis. If one of the Routes is for example invalid it is easy to remove it. */
/* package */ class AvRouteHandler {

    private final NavigableMap<Double, Map<RoboTaxi, Set<SharedAvRoute>>> routes = new TreeMap<>();

    /** Adds a @{@link SharedAvRoute} associated with a RoboTaxi {@link RoboTaxi} to the Tree Structure sorted by its end time and the RoboTaxi.
     * 
     * @param roboTaxi
     * @param sharedAvRoute */
    void add(RoboTaxi roboTaxi, SharedAvRoute sharedAvRoute) {
        double endTime = sharedAvRoute.getEndTime();

        routes.computeIfAbsent(endTime, t -> new HashMap<>()) //
                /* routes.get(endTime) */ .computeIfAbsent(roboTaxi, rt -> new HashSet<>()) //
                /* routes.get(endTime).get(roboTaxi) */ .add(sharedAvRoute);
    }

    /** Removes all {@link SharedAvRoute}s where {@link SharedAvRoute#getEndTime} equals the given {@param endTime};
     * 
     * @param endTime */
    void remove(double endTime) {
        routes.remove(endTime);
    }

    /** Checks if the given {@param endTime} exists in the tree structure
     * 
     * @param endTime
     * @return true if it exists, false else. */
    boolean contains(double endTime) {
        return routes.containsKey(endTime);
    }

    /** get the routes with the earliest End Time.
     * 
     * @return a Deep Copy of the Key Value Mapping of RoboTaxis to a Set of SharedAvRoutes all with the earliest end time currently present in the internal
     *         Tree
     *         structure */
    Map<RoboTaxi, Set<SharedAvRoute>> getCopyOfNext() {
        return new HashMap<>(routes.firstEntry().getValue());
    }

    /** get the number of Route end times stored
     * 
     * @return a Key Value Mapping of RoboTaxis to a Set of SharedAvRoutes all with the earliest end time currently present in the internal Tree structure */
    int getNumbervalues() {
        return routes.size();
    }

    /** get the routes with the earliest End Time. */
    double getNextvalue() {
        return routes.firstKey();
    }

    boolean isEmpty() {
        return routes.isEmpty();
    }
}
