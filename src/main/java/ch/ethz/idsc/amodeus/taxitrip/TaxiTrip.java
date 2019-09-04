/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.time.LocalDateTime;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.LocalDateTimes;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Sign;

/** The class {@link TaxiTrip} is used to transform taxi trips from databases into scenarios
 * for AMoDeus. It contains the relevant recordings of a typical taxi trip recording. */
// TODO better explanation of entries, localID, pickup,dropofLoc in WGS, renames...
public class TaxiTrip implements Comparable<TaxiTrip> {
    public final Integer localId;
    public final String taxiId;
    public final Tensor pickupLoc;
    public final Tensor dropoffLoc;
    public final Scalar distance;
    public final Scalar waitTime;
    // ---
    public LocalDateTime pickupDate;
    public LocalDateTime dropoffDate;
    public final Scalar duration;

    /** @return a {@link TaxiTrip} for wich the {@link LocalDateTime}s @param pickup
     *         and @param dropoff are used to calculate the {@link Scalar} duration
     * @param id
     * @param taxiId
     * @param pickupLoc
     * @param dropoffLoc
     * @param distance
     * @param waitTime */
    public static TaxiTrip of(Integer id, String taxiId, Tensor pickupLoc, Tensor dropoffLoc, //
            Scalar distance, Scalar waitTime, //
            LocalDateTime pickupDate, LocalDateTime dropoffDate) {
        try {
            return new TaxiTrip(id, taxiId, pickupLoc, dropoffLoc, //
                    distance, waitTime, //
                    pickupDate, dropoffDate, Duration.between(pickupDate, dropoffDate));
        } catch (Exception exception) {
            System.err.println("Possible: pickupDate after dropoff date in generation" + //
                    "of taxi trip..");
            exception.printStackTrace();
            return null;
        }
    }

    /** @return a {@link TaxiTrip} for wich the {@link LocalDateTime} of the dropoff is determined
     *         using the @param pickupDate and the trip @param duration
     * @param id
     * @param taxiId
     * @param pickupLoc
     * @param dropoffLoc
     * @param distance
     * @param waitTime */
    public static TaxiTrip of(Integer id, String taxiId, Tensor pickupLoc, Tensor dropoffLoc, //
            Scalar distance, Scalar waitTime, //
            LocalDateTime pickupDate, Scalar duration) {
        try {
            return new TaxiTrip(id, taxiId, pickupLoc, dropoffLoc, //
                    distance, waitTime, //
                    pickupDate, LocalDateTimes.addTo(pickupDate, duration), //
                    Sign.requirePositiveOrZero(duration));
        } catch (Exception exception) {
            System.err.println("Possible: pickupDate after dropoff date in generation" + //
                    "of taxi trip..");
            exception.printStackTrace();
            return null;
        }
    }

    /** This constructor is private as it allows ambiguous entries, of the triple
     * {pickupDate, dropoffDate, duration} only two are necessary. */
    private TaxiTrip(Integer id, String taxiId, Tensor pickupLoc, Tensor dropoffLoc, //
            Scalar distance, Scalar waitTime, // ,
            LocalDateTime pickupDate, LocalDateTime dropoffDate, Scalar duration) {
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
