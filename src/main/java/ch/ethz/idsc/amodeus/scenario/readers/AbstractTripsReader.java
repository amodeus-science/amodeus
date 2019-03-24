/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.readers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.scenario.time.Duration;
import ch.ethz.idsc.amodeus.scenario.trips.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Norm;

public abstract class AbstractTripsReader extends CsvReader {
    private Map<String, Integer> taxiIds = new HashMap<>();

    public AbstractTripsReader(String delim) {
        super(delim);
    }

    public Stream<TaxiTrip> getTripStream(File file) throws IOException {
        read(file);
        final AtomicInteger tripIds = new AtomicInteger(0);
        return lines().map(line -> {
            int tripId = tripIds.getAndIncrement();
            if (tripId % 1000 == 0)
                System.out.println("trips: " + tripId);
            try {
                String taxiCode = getTaxiCode(line);
                int taxiId = taxiIds.getOrDefault(taxiCode, taxiIds.size());
                taxiIds.put(taxiCode, taxiId);

                LocalDateTime pickupTime = getStartTime(line);
                LocalDateTime dropoffTime = getEndTime(line);
                Scalar durationCompute = Duration.between(pickupTime, dropoffTime);
                Scalar durationDataset = getDuration(line);

                if (Scalars.lessEquals(Quantity.of(0.1, SI.SECOND), Norm._2.of(durationDataset.subtract(durationCompute))))
                    System.err.println("Mismatch between duration recorded in data and computed duration," + //
                    "computed duration using start and end time: " + //
                    pickupTime + " --> " + dropoffTime + " != " + durationDataset);

                TaxiTrip trip = TaxiTrip.of(tripId, Integer.toString(taxiId), getPickupLocation(line), getDropoffLocation(line), //
                        getDistance(line), getWaitingTime(line), pickupTime, dropoffTime);
                return trip;
            } catch (Exception e) {
                System.err.println("discard trip " + tripId + ": [" + IntStream.range(0, headers.size()).mapToObj(i -> //
                headers.get(i) + "=" + line[i]).collect(Collectors.joining(", ")) + "]");
                return null;
            }
        }).filter(Objects::nonNull);
    }

    public int getNumberOfTaxis() {
        return taxiIds.size();
    }

    public abstract String getTaxiCode(String[] line);

    public abstract LocalDateTime getStartTime(String[] line) throws ParseException;

    public abstract LocalDateTime getEndTime(String[] line) throws ParseException;

    public abstract Coord getPickupLocation(String[] line);

    public abstract Coord getDropoffLocation(String[] line);

    public abstract Scalar getDuration(String[] line);

    public abstract Scalar getDistance(String[] line);

    public abstract Scalar getWaitingTime(String[] line);
}
