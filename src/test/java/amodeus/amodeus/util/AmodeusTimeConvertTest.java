/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Test;

public class AmodeusTimeConvertTest {

    @Test
    public void test() {
        /** a time stamp from the San Francisco dataset is defined and transformed
         * with a {@link AmodeusTimeconvert} */
        long someLong = 1213035317;
        AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));
        {
            LocalDateTime ldt = timeConvert.getLdt(someLong);
            Assert.assertEquals(2008, ldt.getYear());
            Assert.assertEquals(6, ldt.getMonthValue());
            Assert.assertEquals(9, ldt.getDayOfMonth());
            Assert.assertEquals(11, ldt.getHour());
            Assert.assertEquals(15, ldt.getMinute());
            Assert.assertEquals(17, ldt.getSecond());
            Assert.assertEquals(someLong, timeConvert.toEpochSec(ldt));
        }
        {
            LocalDate localDate = timeConvert.toLocalDate(String.valueOf(someLong));
            Assert.assertEquals(2008, localDate.getYear());
            Assert.assertEquals(6, localDate.getMonthValue());
            Assert.assertEquals(9, localDate.getDayOfMonth());
        }
        {
            Assert.assertEquals("2008-06-09T11:15:17 (1213035317)", timeConvert.toString(someLong));
        }
    }
}
