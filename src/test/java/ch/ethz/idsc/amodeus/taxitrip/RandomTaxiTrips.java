package ch.ethz.idsc.amodeus.taxitrip;

import java.time.LocalDateTime;
import java.util.Random;

import ch.ethz.idsc.amodeus.util.LocalDateTimes;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class RandomTaxiTrips {

    private int globalCount = 0;

    public TaxiTrip createAny() {
        Random random = new Random();
        String tripId = "trip_" + Integer.toString((++globalCount));
        return create(random, tripId);
    }

    public TaxiTrip createSpecific(long seed, String tripId) {
        Random random = new Random(seed);
        return create(random, tripId);
    }

    private TaxiTrip create(Random random, String tripId) {
        String taxiId = "taxi_" + Integer.toString(random.nextInt(100));
        Tensor pickupLoc = Tensors.vector(random.nextDouble(), random.nextDouble());
        Tensor dropoffLoc = Tensors.vector(random.nextDouble(), random.nextDouble());
        Scalar distance = Quantity.of(random.nextDouble(), "m");
        LocalDateTime submissionTime = LocalDateTime.of(random.nextInt(5) + 2019, random.nextInt(12), //
                random.nextInt(29), random.nextInt(24), random.nextInt(60));
        Scalar waitTime = Quantity.of(random.nextInt(500), "s");
        LocalDateTime pickupDate = LocalDateTimes.addTo(submissionTime, waitTime);
        Scalar driveTime = Quantity.of(random.nextInt(500), "s");
        LocalDateTime dropoffDate = LocalDateTimes.addTo(submissionTime, driveTime);
        TaxiTrip randomTrip = TaxiTrip.of(//
                tripId, //
                taxiId, //
                pickupLoc, dropoffLoc, distance, //
                submissionTime, pickupDate, dropoffDate);
        return randomTrip;
    }
}
