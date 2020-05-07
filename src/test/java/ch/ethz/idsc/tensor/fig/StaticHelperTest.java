package ch.ethz.idsc.tensor.fig;

import java.util.Calendar;
import java.util.Random;

import org.jfree.data.time.Second;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import junit.framework.TestCase;

public class StaticHelperTest extends TestCase {
    public void testToTime() {
        Random random = new Random();
        int hour = random.nextInt(696);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1; // Month are 0 based, thus it is necessary to add 1
        Second expected = new Second(second, minute, hour % 24, hour / 24 + 1, month, year);
        Scalar time = RealScalar.of((hour * 60 + minute) * 60 + second);

        Second resulting = StaticHelper.toTime(time);
        assertEquals(expected.toString(), resulting.toString());
    }
}
