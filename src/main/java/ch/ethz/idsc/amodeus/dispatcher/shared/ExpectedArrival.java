/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.router.AVRouter;

public enum ExpectedArrival {
    ;

    /** @param roboTaxi
     * @param now
     * @param router */
    public static void print(RoboTaxi roboTaxi, double now, AVRouter router) {
        System.out.println("***");
        System.out.println("schedule of roboTaxi " + roboTaxi.getId() + " at " + now + ":");
        Map<String, Scalar> expDropOff = of(roboTaxi, now, router);
        for (Entry<String, Scalar> entry : expDropOff.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("***");
    }

    /** @param roboTaxi
     * @param now
     * @return {@link Map} containing the {@link SharedCourse} ids and the expected
     *         time for completion of the {@link SharedCourse} */
    public static Map<String, Scalar> of(RoboTaxi roboTaxi, double now, AVRouter router) {
        Map<String, Scalar> expDropoff = new HashMap<>();
        Scalar time = Quantity.of(now, SI.SECOND);
        Link linkCurr = roboTaxi.getDivertableLocation();
        boolean failFlag = false;
        for (SharedCourse course : roboTaxi.getUnmodifiableViewOfCourses()) {
            Scalar timeTo = timeFromTo(linkCurr, course.getLink(), time, roboTaxi, router);
            if (Objects.isNull(timeTo))
                failFlag = true;
            if (!failFlag) {
                time = time.add(timeTo);
                linkCurr = course.getLink();
                expDropoff.put(course.getCourseId(), time);
            } else {
                expDropoff.put(course.getCourseId(), null);
            }
        }
        return expDropoff;
    }

    /** @return time in seconds needed for {@link RoboTaxi} @param roboTaxi to travel from {@link Link}
     * @param from to the {@link Link} @param to starting at {@link Scalar} @param now and using
     *            the {@link AVRouter} @param router
     * @return null if path calculation unsuccessful */
    private static Scalar timeFromTo(Link from, Link to, Scalar now, RoboTaxi roboTaxi, AVRouter router) {
        Future<Path> path = router.calcLeastCostPath(from.getFromNode(), to.getToNode(), now.number().doubleValue(), //
                null, null);
        Double travelTime = null;
        try {
            travelTime = path.get().travelTime;
        } catch (Exception e) {
            System.err.println("Calculation of expected arrival failed.");
        }
        return Quantity.of(travelTime, SI.SECOND);
    }
}
