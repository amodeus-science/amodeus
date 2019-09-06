package ch.ethz.idsc.amodeus.util;

import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;

import org.junit.Test;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class UnixEpochTest {

    private static final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));

    @Test
    public void testToAmodeus() {

        LocalDate localDate = LocalDate.of(2008, 05, 17);
        int tinit = 1211018404;

        /** initial tests to see if date change works */
        int t1 = tinit + 1000;
        int t2 = tinit + 90000;
        int t3 = tinit + 180000;
        int t4 = 1213167590;

        if (timeConvert.toAmodeus(t1, localDate) != 11804)
            fail("UnixEpoch to Amodeus converter broken.");

        if (timeConvert.toAmodeus(t2, localDate) != 100804)
            fail("UnixEpoch to Amodeus converter broken.");

        if (timeConvert.toAmodeus(t3, localDate) != 190804)
            fail("UnixEpoch to Amodeus converter broken.");

        if (timeConvert.toAmodeus(t4, localDate) != 2159990)
            fail("UnixEpoch to Amodeus converter broken.");

    }

    @Test
    public void testLocalDate() throws Exception {
        Random random = new Random(12345);
        for (int i = 0; i < 1000; ++i) {
            int year = random.nextInt(2020) + 1;
            int month = random.nextInt(12) + 1;
            int day = random.nextInt(28) + 1;
            LocalDate localDate = LocalDate.of(year, month, day);
            Scalar dayLength = Duration.between(timeConvert.beginOf(localDate), timeConvert.endOf(localDate));
            Scalar dayLengthShould = Quantity.of(86399, "s");
            if (!dayLength.equals(dayLengthShould)) {
                System.err.println(dayLength + " =daylength is not " + dayLengthShould);
                fail("UnixEpoch to Amodeus converter broken.");
            }
        }

    }

}
