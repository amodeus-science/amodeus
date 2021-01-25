package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.utils.collections.Tuple;

public class DefaultTravelTimeCalculator implements TravelTimeCalculator {
    private final LeastCostPathCalculator router;
    private final Map<Tuple<Link, Link>, Double> cache = new HashMap<>();

    public DefaultTravelTimeCalculator(LeastCostPathCalculator router) {
        this.router = router;
    }

    @Override
    public double getTravelTime(double departureTime, Link originLink, Link destinationLink) {
        Tuple<Link, Link> tuple = new Tuple<>(originLink, destinationLink);
        Double travelTime = cache.get(tuple);

        if (travelTime != null) {
            return travelTime;
        } else {
            Path path = router.calcLeastCostPath(originLink.getToNode(), destinationLink.getFromNode(), departureTime, null, null);
            cache.put(tuple, path.travelTime);
            return path.travelTime;
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
