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
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class TimeWindowCheck {

    /** @return true if the {@link List}<{@linkSharedCourse}> @param newMenu does not
     *         violate the maximum pickup and drop-off delays tolerated specified in
     *         the {@link Scalar} values @param pickupDelayMax and @param drpoffDelayMax,
     *         for the domputation the current time @param timeNow,
     *         the {@link NetworkTimeDistInterface} @param travelTimeCashed and
     *         a {@link List} @param startLocation are needed */
    public static boolean of(double timeNow, List<Directive> newMenu, //
            NetworkTimeDistInterface travelTimeCashed, Link startLocation, //
            Scalar pickupDelayMax, Scalar drpoffDelayMax) {
        boolean timeComp = true;
        Scalar timePrev = Quantity.of(timeNow, SI.SECOND);

        for (Directive course : newMenu) {
            Link courseLink = null;
            
            if (course instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) course;
                
                if (stopDirective.isPickup()) {
                    courseLink = stopDirective.getRequest().getFromLink();
                } else {
                    courseLink = stopDirective.getRequest().getToLink();
                }
            } else {
                courseLink = ((DriveDirective) course).getDestination();
            }
            
            Scalar travelTime = //
                    travelTimeCashed.travelTime(startLocation, courseLink, timeNow);
            Scalar timeofCourse = timePrev.add(travelTime);
            
            if (course instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) course;
                
                if (stopDirective.isPickup()) {
                    Scalar latestPickup = LatestPickup.of(stopDirective.getRequest(), pickupDelayMax);
                    if (Scalars.lessThan(latestPickup, timeofCourse)) {
                        timeComp = false;
                        break;
                    }
                } else {
                    Scalar latestDropoff = //
                            LatestArrival.of(stopDirective.getRequest(), drpoffDelayMax, travelTimeCashed, timeNow);
                    if (Scalars.lessThan(latestDropoff, timeofCourse)) {
                        timeComp = false;
                        break;
                    }
                }
            }
            
            timePrev = timeofCourse;
        }
        return timeComp;
    }
}
