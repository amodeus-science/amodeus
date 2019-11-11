/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;

public class CachedNetworkPropertyComputation<T> {

    // ---
    private final LeastCostPathCalculator calculator;
    private final Map<Link, Map<Link, T>> cache = new HashMap<>();
    private final NavigableMap<Double, Map<Link, Set<Link>>> calculationTimes = new TreeMap<>();
    private final double maxLag;
    private final NetworkPropertyInterface<T> pathInterface;
    // ---
    protected double now = 0.0;

    /** A {@link CachedNetworkPropertyComputation} stores all the calculated values calculated by a
     * {@link NetworkPropertyInterface} which were calculated no longer ago than @param maxLag. The
     * underlying logic is that in this manner the expensive routing computation has to be done
     * fewer times for identical pairs of {@link Link}s.For the routing, different
     * {@link LeastCostPathCalculator}s can be used, e.g., to minimize travel time or network distance. */
    public CachedNetworkPropertyComputation(LeastCostPathCalculator calculator, double maxLag, //
            NetworkPropertyInterface<T> pathInterface) {
        this.calculator = calculator;
        this.maxLag = maxLag;
        this.pathInterface = pathInterface;
    }

    /** removes computations that happened more time than @param maxLag ago since @param now */
    private final void update(double now) {
        this.now = now;
        Set<Double> timestoRemove = new HashSet<>();
        calculationTimes.headMap(now - maxLag).forEach((time, map) -> {
            // GlobalAssert.that(time < now - maxLag); // should be guaranteed by headMap
            timestoRemove.add(time);
            map.forEach((fromLink, set) -> set.forEach(cache.get(fromLink)::remove));
        });
        timestoRemove.forEach(calculationTimes::remove);
    }

    public final T fromTo(Link from, Link to, double now) {
        update(now);
        return cache.computeIfAbsent(from, l -> new HashMap<>()) //
            /* cache.get(from) */ .computeIfAbsent(to, l -> {
            addToCalculationTime(now, from, l);
            return pathInterface.fromTo(from, l, calculator, now);
        });
    }

    private final void addToCalculationTime(double now, Link from, Link to) {
        calculationTimes.computeIfAbsent(now, t -> new HashMap<>()) //
            /* calculationTimes.get(now) */ .computeIfAbsent(from, l -> new HashSet<>()) //
                /* calculationTimes.get(now).get(from) */ .add(to);
    }

    public boolean checkTime(double now) {
        return this.now == now;
    }
}
