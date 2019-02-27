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
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

public class CashedPathComputation<T> {

    // ---
    private final LeastCostPathCalculator calculator;
    private final Map<Link, Map<Link, T>> cache = new HashMap<>();
    private final NavigableMap<Double, Map<Link, Set<Link>>> calculationTimes = new TreeMap<>();
    private final double maxLag;
    private final PathInterface<T> pathInterface;
    // ---
    protected double now = 0.0;

    /** A {@link CashedPathComputation} stores all the calculated travel times
     * which were calculated no longer ago than @param maxLag. The underlying logic is that in this manner
     * the expensive routing computation has to be done fewer times for identical pairs
     * of {@link Link}s.For the routing, different {@link LeastCostPathCalculator}s can be used,
     * e.g., to minimize traveltime or network distance. If the boolean @param cachePath is set to
     * true, then the computed Pathes are stored as well (memory intensive!) */
    public CashedPathComputation(LeastCostPathCalculator calculator, Double maxLag, PathInterface<T> pathInterface) {
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

    protected final T fromTo(Link from, Link to) {
        if (!cache.containsKey(from))
            cache.put(from, new HashMap<>());
        if (cache.get(from).containsKey(to))
            return cache.get(from).get(to);
        T timeDist = pathInterface.fromTo(from, to, calculator, now);// timePathComputation(from, to);
        cache.get(from).put(to, timeDist);
        addToCalculationTime(now, from, to);
        return timeDist;
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

    // public boolean isForNow(double now) {
    // return this.now == now;
    // }

    private Tensor timePathComputation(Link from, Link to) {
        /** path */
        Path path = calculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), now, null, null);
        /** travel time */
        Scalar travelTime = Quantity.of(path.travelTime, SI.SECOND);
        /** path length */
        double dist = 0.0;
        for (Link link : path.links)
            dist += link.getLength();
        Scalar distance = Quantity.of(dist, SI.METER);
        /** return pair */
        return Tensors.of(travelTime, distance);
    }

    // @Override
    // public Scalar travelTime(Link from, Link to) {
    // Tensor timeDist = fromTo(from, to);
    // return timeDist.Get(0);
    // }
    //
    // @Override
    // public Scalar distance(Link from, Link to) {
    // Tensor timeDist = fromTo(from, to);
    // return timeDist.Get(1);
    // }
}
