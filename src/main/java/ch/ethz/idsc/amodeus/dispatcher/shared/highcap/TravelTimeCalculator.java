package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

/* package */ class TravelTimeCalculator {

    private final LeastCostPathCalculator lcpc;
    private final LimitedSizeMap<Link, Map<Link, Double>> travelTimeDataMap;

    public TravelTimeCalculator(LeastCostPathCalculator lcpc, int sizeLimit) {
        this.lcpc = lcpc;
        travelTimeDataMap = new LimitedSizeMap<>(sizeLimit);
    }

    // map data structure
    double of(Link fromLink, Link toLink, double now, boolean storeInCache) {
        if (!travelTimeDataMap.containsKey(fromLink)) 

            travelTimeDataMap.put(fromLink, new HashMap<>());

        if (travelTimeDataMap.get(fromLink).containsKey(toLink))
            return travelTimeDataMap.get(fromLink).get(toLink);

        // if it reaches here, we need to calculate the travel time
        Path shortest = lcpc.calcLeastCostPath(fromLink.getFromNode(), toLink.getToNode(), now, null, null);
        if (storeInCache)
            travelTimeDataMap.get(fromLink).put(toLink, shortest.travelTime);

        return shortest.travelTime;
    }

    void clearDataMap() {
        travelTimeDataMap.clear();
    }

    void storeInCache(Link fromLink, Link toLink, double travelTime) {
        travelTimeDataMap.get(fromLink).put(toLink, travelTime);
    }

    void removeEntry(Link fromLink) {
        travelTimeDataMap.remove(fromLink);
    }

    int getMapSize() {
        return travelTimeDataMap.size();
    }
}
