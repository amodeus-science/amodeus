/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.util.Objects;

public enum TaxiTripCheck {
    ;

    /** @return true of the {@link TaxiTrip} @param taxiTrip contains the minimally
     *         required information for most processing steps:
     *         pickup and dropoff date and time. */
    public static boolean isOfMinimalScope(TaxiTrip taxiTrip) {
        return Objects.nonNull(taxiTrip.duration) && //
                Objects.nonNull(taxiTrip.pickupDate) && //
                Objects.nonNull(taxiTrip.dropoffDate) && //
                Objects.nonNull(taxiTrip.pickupLoc) && //
                Objects.nonNull(taxiTrip.dropoffLoc);
    }
}
