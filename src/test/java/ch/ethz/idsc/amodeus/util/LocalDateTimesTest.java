/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.qty.Quantity;

public class LocalDateTimesTest {

    @Test
    public void test() {
        LocalDateTime ldt = LocalDateTime.of(2008, 12, 24, 20, 05, 17);
        LocalDateTime ldt2 = LocalDateTimes.addTo(ldt, Quantity.of(63, SI.SECOND));
        assertEquals(ldt2.toString(), "2008-12-24T20:06:20");
    }

}
