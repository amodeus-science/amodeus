/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

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
import ch.ethz.idsc.tensor.qty.Quantity;

/** A TravelTimeCalculatorCached stores all the calculated travel times
 * which were calculated within the specified Timelag duration.
 * Like that a very efficient travel time calculation can be guaranteed such that
 * the computationally expensive routing has only to be done once for a given link
 * to link pair. */
// TODO move to separate class and ensure that duplicate code with
// CashedDistanceCalculator is removed.
public class TravelTimeCalculatorCached implements TravelTimeCalculator {

    public static TravelTimeCalculatorCached of(LeastCostPathCalculator calculator, double maxLag) {
        return new TravelTimeCalculatorCached(calculator, maxLag);
    }

    // ---
    private final LeastCostPathCalculator calculator;
    private final Map<Link, Map<Link, Scalar>> db = new HashMap<>();
    private final NavigableMap<Double, Map<Link, Set<Link>>> calculationTimes = new TreeMap<>();
    private final double maxLag;
    // ---
    private double now = 0.0;

    private TravelTimeCalculatorCached(LeastCostPathCalculator calculator, Double maxLag) {
        this.calculator = calculator;
        this.maxLag = maxLag;
    }

    public void update(Double now) {
        this.now = now;
        Set<Double> timestoRemove = new HashSet<>();
        for (Entry<Double, Map<Link, Set<Link>>> entry : calculationTimes.headMap(now - maxLag).entrySet()) {
            GlobalAssert.that(entry.getKey() < now - maxLag);
            timestoRemove.add(entry.getKey());
            for (Entry<Link, Set<Link>> fromentry : entry.getValue().entrySet()) {
                fromentry.getValue().forEach(to -> db.get(fromentry.getKey()).remove(to));
            }
        }
        timestoRemove.forEach(time -> calculationTimes.remove(time));
    }

    @Override
    public Scalar timeFromTo(Link from, Link to) {
        if (!db.containsKey(from))
            db.put(from, new HashMap<>());
        if (db.get(from).containsKey(to))
            return db.get(from).get(to);
        Scalar time = timeFromTo(from, to, now, calculator);
        db.get(from).put(to, time);
        addToCalculationTime(now, from, to);
        return time;
    }

    private static Scalar timeFromTo(Link from, Link to, Double now, LeastCostPathCalculator calculator) {
        Path path = calculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), now, null, null);
        return Quantity.of(path.travelTime, SI.SECOND);
    }

    private void addToCalculationTime(Double now, Link from, Link to) {
        if (!calculationTimes.containsKey(now))
            calculationTimes.put(now, new HashMap<>());

        if (!calculationTimes.get(now).containsKey(from))
            calculationTimes.get(now).put(from, new HashSet<>());

        calculationTimes.get(now).get(from).add(to);
    }

    @Override
    public boolean isForNow(double now) {
        return this.now == now;
    }

}
