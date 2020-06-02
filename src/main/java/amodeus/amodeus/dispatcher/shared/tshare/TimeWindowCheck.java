/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.tshare;

import java.util.List;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedMealType;
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
    public static boolean of(double timeNow, List<SharedCourse> newMenu, //
            NetworkTimeDistInterface travelTimeCashed, Link startLocation, //
            Scalar pickupDelayMax, Scalar drpoffDelayMax) {
        boolean timeComp = true;
        Scalar timePrev = Quantity.of(timeNow, SI.SECOND);

        for (SharedCourse course : newMenu) {
            Scalar travelTime = //
                    travelTimeCashed.travelTime(startLocation, course.getLink(), timeNow);
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
