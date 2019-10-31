package ch.ethz.idsc.amodeus.taxitrip;

import org.junit.Assert;
import org.junit.Test;

public class TaxiTripEqualsTest {

    private static RandomTaxiTrips randomTrips = new RandomTaxiTrips();

    @Test
    public void test() throws IllegalArgumentException, IllegalAccessException {

        {
            TaxiTrip t1 = randomTrips.createSpecific(1, "trip1");
            TaxiTrip t2 = randomTrips.createSpecific(2, "trip2");
            // must be false
            Assert.assertTrue(!t1.equals(t2));
        }

        {
            TaxiTrip t1 = randomTrips.createSpecific(1, "trip1");
            TaxiTrip t2 = randomTrips.createSpecific(1, "trip2");
            // must be false
            Assert.assertTrue(!t1.equals(t2));
        }

        {
            TaxiTrip t1 = randomTrips.createSpecific(1, "trip1");
            TaxiTrip t2 = randomTrips.createSpecific(1, "trip1");
            // must be false
            Assert.assertTrue(t1.equals(t2));
        }
    }
}
