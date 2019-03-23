package ch.ethz.idsc.amodeus.scenario.time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum Duration {
    ;

    /** @return {@link Scalar} duration of interval [@param ldt1, @param ldt2] in [s]
     * @throws Exception */
    public static Scalar between(LocalDateTime ldt1, LocalDateTime ldt2) throws Exception {
        if (LocalDateTimes.lessThan(ldt2, ldt1))
            throw new Exception(ldt1.toString() + " is after " + ldt2.toString() //
                    + ": cannot compute duration.");
        long sec1 = ldt1.toEpochSecond(ZoneOffset.of("Z"));
        long sec2 = ldt2.toEpochSecond(ZoneOffset.of("Z"));
        return Quantity.of(sec2 - sec1, "s");
    }

}
