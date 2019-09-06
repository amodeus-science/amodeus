package ch.ethz.idsc.amodeus.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Test;

public class UnixEpochDateTest {

    @Test
    public void test() {
        /** a time stamp from the San Francisco dataset is defined and transformed
         * with a {@link AmodeusTimeconvert} */
        int someInt = 1213035317;
        AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));
        LocalDateTime ldt = timeConvert.getLdt(someInt);
        Assert.assertEquals(2008, ldt.getYear());
        Assert.assertEquals(6, ldt.getMonthValue());
        Assert.assertEquals(9, ldt.getDayOfMonth());
        Assert.assertEquals(11, ldt.getHour());
        Assert.assertEquals(15, ldt.getMinute());
        Assert.assertEquals(17, ldt.getSecond());
    }
}
