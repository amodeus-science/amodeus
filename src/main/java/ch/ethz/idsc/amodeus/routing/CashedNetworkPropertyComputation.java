package ch.ethz.idsc.amodeus.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class CashedNetworkPropertyComputation<T> {

    // ---
    private final LeastCostPathCalculator calculator;
    private final Map<Link, Map<Link, T>> cache = new HashMap<>();
    private final NavigableMap<Double, Map<Link, Set<Link>>> calculationTimes = new TreeMap<>();
    private final double maxLag;
    private final NetworkPropertyInterface<T> pathInterface;
    // ---
    protected double now = 0.0;

    /** A {@link CashedNetworkPropertyComputation} stores all the calculated values calculated by a
     * {@link NetworkPropertyInterface} which were calculated no longer ago than @param maxLag. The
     * underlying logic is that in this manner the expensive routing computation has to be done
     * fewer times for identical pairs of {@link Link}s.For the routing, different
     * {@link LeastCostPathCalculator}s can be used, e.g., to minimize travel time or network distance. */
    public CashedNetworkPropertyComputation(LeastCostPathCalculator calculator, Double maxLag, //
            NetworkPropertyInterface<T> pathInterface) {
        this.calculator = calculator;
        this.maxLag = maxLag;
        this.pathInterface = pathInterface;
    }

    /** removes computations that happened more time than @param maxLag ago since @param now */
    public final void update(Double now) {
        this.now = now;
        Set<Double> timestoRemove = new HashSet<>();
        for (Entry<Double, Map<Link, Set<Link>>> entry : calculationTimes.headMap(now - maxLag).entrySet()) {
            GlobalAssert.that(entry.getKey() < now - maxLag);
            timestoRemove.add(entry.getKey());
            for (Entry<Link, Set<Link>> fromentry : entry.getValue().entrySet()) {
                fromentry.getValue().forEach(to -> cache.get(fromentry.getKey()).remove(to));
            }
        }
        timestoRemove.forEach(time -> calculationTimes.remove(time));
    }

    public final T fromTo(Link from, Link to) {
        if (!cache.containsKey(from))
            cache.put(from, new HashMap<>());
        if (cache.get(from).containsKey(to))
            return cache.get(from).get(to);
        T t = pathInterface.fromTo(from, to, calculator, now);// timePathComputation(from, to);
        cache.get(from).put(to, t);
        addToCalculationTime(now, from, to);
        return t;
    }

    private final void addToCalculationTime(Double now, Link from, Link to) {
        if (!calculationTimes.containsKey(now))
            calculationTimes.put(now, new HashMap<>());
        if (!calculationTimes.get(now).containsKey(from))
            calculationTimes.get(now).put(from, new HashSet<>());
        calculationTimes.get(now).get(from).add(to);
    }

    public boolean checkTime(double now) {
        return this.now == now;
    }

}
