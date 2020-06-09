/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.taxitrip;

import java.util.Objects;

public enum TaxiTripCheck {
    ;

    /** @return true of the {@link TaxiTrip} @param taxiTrip contains the minimally
     *         required information for most processing steps:
     *         pickup and dropoff date and time. */
    public static boolean isOfMinimalScope(TaxiTrip taxiTrip) {
        return Objects.nonNull(taxiTrip.driveTime) && //
                Objects.nonNull(taxiTrip.pickupTimeDate) && //
                Objects.nonNull(taxiTrip.dropoffTimeDate) && //
                Objects.nonNull(taxiTrip.pickupLoc) && //
                Objects.nonNull(taxiTrip.dropoffLoc);
    }
}
