/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class LeastCostCalculatorDatabaseOneTime {

    /* package */ static LeastCostCalculatorDatabaseOneTime of(LeastCostPathCalculator calculator, Scalar now) {
        return new LeastCostCalculatorDatabaseOneTime(calculator, now);
    }

    private final LeastCostPathCalculator calculator;

    private final Map<Link, Map<Link, Scalar>> db = new HashMap<>();
    private final Scalar now;

    private LeastCostCalculatorDatabaseOneTime(LeastCostPathCalculator calculator, Scalar now) {
        this.calculator = calculator;
        this.now = now;
    }

    /* package */ Scalar timeFromTo(Link from, Link to) {
        if (!db.containsKey(from))
            db.put(from, new HashMap<>());
        if (db.get(from).containsKey(to))
            return db.get(from).get(to);
        Scalar time = timeFromTo(from, to, now, calculator);
        db.get(from).put(to, time);
        return time;
    }

    private static Scalar timeFromTo(Link from, Link to, Scalar now, LeastCostPathCalculator calculator) {
        Path path = calculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), now.number().doubleValue(), //
                null, null);
        Double travelTime = null;
        try {
            travelTime = path.travelTime;
        } catch (Exception e) {
            System.err.println("Calculation of expected arrival failed.");
        }
        return Quantity.of(travelTime, SI.SECOND);
    }

    /* package */ boolean isForNow(Scalar now) {
        return this.now.equals(now);
    }
}
