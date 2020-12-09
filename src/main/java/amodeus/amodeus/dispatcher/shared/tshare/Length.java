/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.tshare;

import java.util.List;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.DriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;
import amodeus.amodeus.routing.NetworkTimeDistInterface;
import amodeus.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ enum Length {
    ;

    /** @return {@link Scalar} drive distance to cover all {@link SharedCourse}s in the
     *         {@link List} @param menu from the {@link Link} start @param startLocation. For the
     *         computation the {@link NetworkTimeDistInterface} @param networkTimeDistInterface at
     *         time {@link Double} @param now is used. */
    public static Scalar of(Link startLocation, List<Directive> menu, //
            NetworkTimeDistInterface networkTimeDistInterface, double now) {
        if (menu.isEmpty())
            return Quantity.of(0.0, SI.METER);
        Link link = startLocation;
        Scalar length = Quantity.of(0.0, SI.METER);
        for (Directive course : menu) {
            Link courseLink = null;
            
            if (course instanceof DriveDirective) {
                courseLink = ((DriveDirective) course).getDestination();
            } else {
                StopDirective stop = (StopDirective) course;
                
                if (stop.isPickup()) {
                    courseLink = stop.getRequest().getFromLink();
                } else {
                    courseLink = stop.getRequest().getToLink();
                }
            }
            
            length = length.add(networkTimeDistInterface.distance(link, courseLink, now));
            link = courseLink;
        }
        return length;
    }
}
