package ch.ethz.idsc.amodeus.scenario.time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum LocalDateTimes {
    ;

    /** @return true if the {@link LocalDateTime} @param ldt1 is less than or equal to
     *         the {@link LocalDateTime} @param ldt2 and false otherwise. */
    public static boolean lessEquals(LocalDateTime ldt1, LocalDateTime ldt2) {
        return lessThan(ldt1, ldt2) || ldt1.toInstant(ZoneOffset.UTC).toEpochMilli() == //
        ldt2.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /** @return true if the {@link LocalDateTime} @param ldt1 is strictly less than
     *         the {@link LocalDateTime} @param ldt2 and false otherwise. */
    public static boolean lessThan(LocalDateTime ldt1, LocalDateTime ldt2) {
        return ldt1.toInstant(ZoneOffset.UTC).toEpochMilli() < //
        ldt2.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /** @return the {@link LocalDateTime} @param ldt to which @param seconds were added, works
     *         also for negative amounts of seconds */
    public static LocalDateTime addTo(LocalDateTime ldt, Scalar seconds) {
        GlobalAssert.that(((Quantity) seconds).unit().equals(SI.SECOND));
        long epochSecond = ldt.toInstant(ZoneOffset.UTC).toEpochMilli() / 1000 + seconds.number().intValue();
        int nanoRest = (int) (ldt.toInstant(ZoneOffset.UTC).toEpochMilli() % 1000 * 1000000);
        return LocalDateTime.ofEpochSecond(epochSecond, nanoRest, ZoneOffset.UTC);
    }

    /** @return the {@link LocalDateTime} @param ldt to which @param seconds were subtracted. */
    public static LocalDateTime subtractFrom(LocalDateTime ldt, Scalar seconds) {
        return addTo(ldt, seconds);
    }

    // TODO move to tests
    public static void main(String[] args) {
        LocalDateTime ldt = LocalDateTime.of(2008, 12, 24, 20, 05, 17);
        LocalDateTime ldt2 = addTo(ldt, Quantity.of(63,"s"));
        System.out.println(ldt);
        System.out.println(ldt2);
    }

}
