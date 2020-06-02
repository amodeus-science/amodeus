/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import amodeus.amodeus.util.math.Magnitude;
import ch.ethz.idsc.tensor.Scalar;

public enum LocalDateTimes {
    ;

    /** @return true if the {@link LocalDateTime} @param ldt1 is less than or equal to
     *         the {@link LocalDateTime} @param ldt2 and false otherwise. */
    @Deprecated // (almost) redundant, see below
    public static boolean lessEquals(LocalDateTime ldt1, LocalDateTime ldt2) {
        // return lessThan(ldt1, ldt2) //
        // || ldt1.toInstant(ZoneOffset.UTC).toEpochMilli() == ldt2.toInstant(ZoneOffset.UTC).toEpochMilli();
        return ldt1.isBefore(ldt2) || ldt1.isEqual(ldt2);
    }

    /** @return true if the {@link LocalDateTime} @param ldt1 is strictly less than
     *         the {@link LocalDateTime} @param ldt2 and false otherwise. */
    @Deprecated // redundant, see below
    public static boolean lessThan(LocalDateTime ldt1, LocalDateTime ldt2) {
        // return ldt1.toInstant(ZoneOffset.UTC).toEpochMilli() < //
        // ldt2.toInstant(ZoneOffset.UTC).toEpochMilli();
        return ldt1.isBefore(ldt2);
    }

    /** @return the {@link LocalDateTime} @param localDateTime to which @param duration were added, works
     *         also for negative duration */
    public static LocalDateTime addTo(LocalDateTime localDateTime, Scalar duration) {
        Scalar seconds = Magnitude.SECOND.apply(duration);
        long epochSecond = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli() / 1000 + seconds.number().longValue();
        int nanoRest = (int) (localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli() % 1000 * 1000000);
        return LocalDateTime.ofEpochSecond(epochSecond, nanoRest, ZoneOffset.UTC);
    }

    /** @return the {@link LocalDateTime} @param ldt to which @param duration were subtracted. */
    public static LocalDateTime subtractFrom(LocalDateTime localDateTime, Scalar duration) {
        return addTo(localDateTime, duration.negate());
    }
}
