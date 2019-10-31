/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.util.Calendar;

import org.jfree.data.time.Second;

import junit.framework.TestCase;

public class StaticHelperTest extends TestCase {

    public void testSimple() {
        double time = 150.2;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1; // Month are 0 indexed in java

        Second sec = StaticHelper.toTime(time);
        Second sec2 = new Second(30, 2, 0, 1, month, year);
        assertEquals(sec, sec2);
    }
}
