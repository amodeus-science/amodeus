package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;

/* package */ enum Length {
    ;

    public static Double of(RoboTaxi roboTaxi, List<SharedCourse> menu, NetworkDistanceFunction distance) {
        if (menu.isEmpty())
            return 0.0;

        double length = 0.0;
        Link link = roboTaxi.getDivertableLocation();
        for (SharedCourse course : menu) {
            length += distance.getDistance(link, course.getLink());
            link = course.getLink();
        }
        return length;
    }
}
