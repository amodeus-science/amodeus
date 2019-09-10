/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.time.LocalDate;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.LocalDateTimes;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum PathHandlerUtil {
    ;

    public static void validityCheck(TaxiTrip taxiTrip, AmodeusTimeConvert timeConvert, //
            LocalDate simulationDate, ClosestLinkSelect linkSelect) {
        int tripStart = timeConvert.ldtToAmodeus(taxiTrip.pickupDate, simulationDate);
        int tripEnd = timeConvert.ldtToAmodeus(taxiTrip.dropoffDate, simulationDate);
        int tripDuration = tripEnd - tripStart;
        GlobalAssert.that(tripDuration >= 0);
        GlobalAssert.that(tripStart >= 0);
        GlobalAssert.that(tripEnd >= 0);
        GlobalAssert.that(LocalDateTimes.lessEquals(simulationDate.atStartOfDay(), taxiTrip.pickupDate));
        GlobalAssert.that(LocalDateTimes.lessEquals(simulationDate.atStartOfDay(), taxiTrip.dropoffDate));
        GlobalAssert.that(LocalDateTimes.lessEquals(taxiTrip.pickupDate, taxiTrip.dropoffDate));
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, "s"), taxiTrip.duration));
        Link pickupLink = linkSelect.linkFromWGS84(taxiTrip.pickupLoc);
        Link dropOffLink = linkSelect.linkFromWGS84(taxiTrip.dropoffLoc);
        Objects.requireNonNull(pickupLink);
        Objects.requireNonNull(dropOffLink);
    }
}
