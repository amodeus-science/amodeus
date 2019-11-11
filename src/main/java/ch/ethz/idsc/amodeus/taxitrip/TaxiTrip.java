/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.LocalDateTimes;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Sign;

/** The class {@link TaxiTrip} is used to transform taxi trips from databases into scenarios
 * for AMoDeus. It contains the relevant recordings of a typical taxi trip recording. */
public class TaxiTrip implements Comparable<TaxiTrip>, Serializable {
    /** @return a {@link TaxiTrip} for wich the {@link LocalDateTime} of the dropoff is determined
     *         using the @param pickupDate the trip @param duration, the {@link LocalDateTime} of the
     *         submission is determined using the @param waitTime. @param id and @param taxiId ideally
     *         should be unique identifiers, @param pickupLoc and @param dropoffLoc are in format
     *         {longitude, latitude}. The @param distance is a distance recorded in the data set,
     *         if available, and null otherwise */
    public static TaxiTrip of(String id, String taxiId, Tensor pickupLoc, Tensor dropoffLoc, Scalar distance, //
            LocalDateTime pickupTimeDate, Scalar waitTime, Scalar driveTime) {
        try {
            LocalDateTime submissionDate = Objects.nonNull(waitTime) ? //
                    LocalDateTimes.subtractFrom(pickupTimeDate, waitTime) : null;
            LocalDateTime dropoffDate = Objects.nonNull(driveTime) ? //
                    LocalDateTimes.addTo(pickupTimeDate, driveTime) : null;

            return new TaxiTrip(id, taxiId, pickupLoc, dropoffLoc, distance, //
                    submissionDate, pickupTimeDate, dropoffDate, //
                    waitTime, Sign.requirePositiveOrZero(driveTime));
        } catch (Exception exception) {
            System.err.println("Possible: pickupDate after dropoff date in generation of taxi trip..");
            exception.printStackTrace();
            return null;
        }
    }

    /** @return a {@link TaxiTrip} for wich the {@link LocalDateTime}s @param pickup
     *         and @param dropoff are used to calculate the {@link Scalar} duration and the wait time.
     *         The @param id and @param taxiId ideally
     *         should be unique identifiers, @param pickupLoc and @param dropoffLoc are in format
     *         {longitude, latitude}. The @param distance is a distance recorded in the data set,
     *         if available, and null otherwise. */
    public static TaxiTrip of(String id, String taxiId, Tensor pickupLoc, Tensor dropoffLoc, Scalar distance, //
            LocalDateTime submissionTimeDate, LocalDateTime pickupTimeDate, LocalDateTime dropoffTimeDate) {
        try {
            Scalar waitTime = Duration.between(submissionTimeDate, pickupTimeDate);
            Scalar duration = Duration.between(pickupTimeDate, dropoffTimeDate);
            return new TaxiTrip(id, taxiId, pickupLoc, dropoffLoc, distance, //
                    submissionTimeDate, pickupTimeDate, dropoffTimeDate, //
                    waitTime, duration);
        } catch (Exception exception) {
            System.err.println("Possible: pickupDate after dropoff date in generation of taxi trip..");
            exception.printStackTrace();
            return null;
        }
    }

    // ---
    /** id allowing to identify individual trips */
    public final String localId;
    /** id allowing to identify individual taxis */
    public final String taxiId;
    public final Tensor pickupLoc; // pickup location in format {longitude,latidue}
    public final Tensor dropoffLoc; // dropoff location in format {longitude,latidue}
    public final Scalar distance; // distance if recorded in data set

    // ---
    public LocalDateTime submissionTimeDate; // trip submission time and date
    public LocalDateTime pickupTimeDate; // trip pickup time and date
    public LocalDateTime dropoffTimeDate; // trip dropoff time and date
    public final Scalar waitTime; // wait time if recorded in data set
    public final Scalar driveTime; // trip drive time

    /** must be private as it allows amigibuous entries, for the 5 time quantities, only three entries
     * are required */
    private TaxiTrip(String id, String taxiId, Tensor pickupLoc, Tensor dropoffLoc, Scalar distance, //
            LocalDateTime submissionTimeDate, LocalDateTime pickupTimeDate, LocalDateTime dropoffTimeDate, //
            Scalar waitTime, Scalar driveTime) {
        this.localId = id;
        this.taxiId = taxiId;
        this.submissionTimeDate = submissionTimeDate;
        this.pickupTimeDate = pickupTimeDate;
        this.dropoffTimeDate = dropoffTimeDate;
        this.pickupLoc = pickupLoc;
        this.dropoffLoc = dropoffLoc;
        this.driveTime = driveTime;
        this.waitTime = waitTime;
        this.distance = distance;
    }

    @Override
    public int compareTo(TaxiTrip trip) {
        return this.pickupTimeDate.compareTo(trip.pickupTimeDate);
    }

    @Override
    public String toString() {
        return Arrays.stream(TaxiTrip.class.getFields()).map(field -> {
            try {
                return field.get(this);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).map(Objects::toString).collect(Collectors.joining("; "));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TaxiTrip))
            return false;
        TaxiTrip other = (TaxiTrip) obj;
        for (Field field : TaxiTrip.class.getFields()) {
            try {
                Object ofMe = field.get(this);
                Object ofOther = field.get(other);
                if (Objects.nonNull(ofMe) && Objects.nonNull(ofOther))
                    if (!ofMe.equals(ofOther))
                        return false;
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
