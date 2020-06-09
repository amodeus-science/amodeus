package amodeus.amodeus.taxitrip;

import org.junit.Assert;
import org.junit.Test;

public class TaxiTripEqualsTest {

    private static RandomTaxiTrips randomTrips = new RandomTaxiTrips();

    @Test
    public void test() throws IllegalArgumentException {

        {
            TaxiTrip t1 = RandomTaxiTrips.createSpecific(1, "trip1");
            TaxiTrip t2 = RandomTaxiTrips.createSpecific(2, "trip2");
            // must be false
            Assert.assertTrue(!t1.equals(t2));
        }

        {
            TaxiTrip t1 = RandomTaxiTrips.createSpecific(1, "trip1");
            TaxiTrip t2 = RandomTaxiTrips.createSpecific(1, "trip2");
            // must be false
            Assert.assertTrue(!t1.equals(t2));
        }

        {
            TaxiTrip t1 = RandomTaxiTrips.createSpecific(1, "trip1");
            TaxiTrip t2 = RandomTaxiTrips.createSpecific(1, "trip1");
            // must be false
            Assert.assertTrue(t1.equals(t2));
        }
    }
}
