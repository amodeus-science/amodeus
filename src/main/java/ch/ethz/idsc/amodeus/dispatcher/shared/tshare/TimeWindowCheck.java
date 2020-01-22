/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.routing.NetworkTimeDistInterface;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class TimeWindowCheck {

    public static boolean of(double timeNow, List<SharedCourse> newMenu, //
            NetworkTimeDistInterface travelTimeCashed, RoboTaxi roboTaxi, //
            Scalar pickupDelayMax, Scalar drpoffDelayMax) {
        boolean timeComp = true;        
        Scalar timePrev = Quantity.of(timeNow, SI.SECOND);
        
        for (SharedCourse course : newMenu) {
            Scalar travelTime = //
                    travelTimeCashed.travelTime(roboTaxi.getLastKnownLocation(), course.getLink(), timeNow);
            Scalar timeofCourse = timePrev.add(travelTime);
            if (course.getMealType().equals(SharedMealType.PICKUP)) {
                Scalar latestPickup = LatestPickup.of(course.getAvRequest(), pickupDelayMax);
                if (Scalars.lessThan(latestPickup, timeofCourse)) {
                    timeComp = false;
                    break;
                }
            }
            if (course.getMealType().equals(SharedMealType.DROPOFF)) {
                Scalar latestDropoff = //
                        LatestArrival.of(course.getAvRequest(), drpoffDelayMax, travelTimeCashed, timeNow);
                if (Scalars.lessThan(latestDropoff, timeofCourse)) {
                    timeComp = false;
                    break;
                }
            }
            timePrev = timeofCourse;
        }
        return timeComp;
    }
}
