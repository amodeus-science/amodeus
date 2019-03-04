/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.time.LocalDateTime;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Scalar;

/** The class {@link TaxiTrip} is used to transform taxi trips from databases into scenarios
 * for AMoDeus. It contains the relevant recordings of a typical taxi trip recording. */
public class TaxiTrip implements Comparable<TaxiTrip> {
    public final Integer localId;
    public final String taxiId;
    public final Coord pickupLoc;
    public final Coord dropoffLoc;
    public final Scalar distance;
    public final Scalar waitTime;
    // ---
    public LocalDateTime pickupDate;
    public LocalDateTime dropoffDate;
    public final Scalar duration;

    public TaxiTrip(Integer id, String taxiId, LocalDateTime pickupDate, LocalDateTime dropoffDate, //
            Coord pickupLoc, Coord dropoffLoc, //
            Scalar duration, Scalar distance, Scalar waitTime) {
        this.localId = id;
        this.taxiId = taxiId;
        this.pickupDate = pickupDate;
        this.dropoffDate = dropoffDate;
        this.pickupLoc = pickupLoc;
        this.dropoffLoc = dropoffLoc;
        this.duration = duration;
        this.distance = distance;
        this.waitTime = waitTime;
    }

    @Override
    public int compareTo(TaxiTrip trip) {
        return this.pickupDate.compareTo(trip.pickupDate);
    }

    @Override
    public String toString() {
        return pickupDate + "\t:\tTrip " + localId + "\t(Taxi " + taxiId + ")";
    }
}
