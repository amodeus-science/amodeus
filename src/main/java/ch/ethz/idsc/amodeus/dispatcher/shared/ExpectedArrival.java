package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum ExpectedArrival {
    ;

    public static Map<String, Scalar> of(RoboTaxi roboTaxi, double now) {
        HashMap<String, Scalar> expDropoff = new HashMap<>();

        SharedMenu menu = roboTaxi.getMenu();
        Scalar time = Quantity.of(now, SI.SECOND);
        Link linkCurr = roboTaxi.getDivertableLocation();
        for (SharedCourse course : menu.getCourses()) {
            Scalar timeTo = timeFromTo(linkCurr, course.getLink());
            time = time.add(timeTo);
            linkCurr = course.getLink();
            expDropoff.put(course.getRequestId(), time);
        }
        
        return expDropoff;
    }

    public static Scalar timeFromTo(Link from, Link to) {
        GlobalAssert.that(false);
        // TODO
        return null;

    }
}
