package ch.ethz.idsc.amodeus.taxitrip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public class TaxiTripImportExportTest {

    private static RandomTaxiTrips randomTrips = new RandomTaxiTrips();

    @Test
    public void test() throws Exception {

        File tripFile = new File(MultiFileTools.getDefaultWorkingDirectory(), "testTrips.csv");

        // 1 create taxi trips
        List<TaxiTrip> someTrips = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            someTrips.add(randomTrips.createAny());
        }

        // 2 export them
        ExportTaxiTrips.toFile(someTrips.stream(), tripFile);

        // 3 import again
        List<TaxiTrip> importedTrips = ImportTaxiTrips.fromFile(tripFile);

        // 4 compare all trips
        for (int i = 0; i < importedTrips.size(); ++i) {
            TaxiTrip original = someTrips.get(i);
            TaxiTrip imported = importedTrips.get(i);
            Assert.assertTrue(original.equals(imported));
        }

        // 5 remove the trip export file
        boolean isDeleted = tripFile.delete();
        Assert.assertTrue(isDeleted);
    }
}
