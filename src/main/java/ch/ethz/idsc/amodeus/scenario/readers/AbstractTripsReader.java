/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.readers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.scenario.trips.TaxiTrip;
import ch.ethz.idsc.tensor.Scalar;

public abstract class AbstractTripsReader extends CsvReader {
    private Map<String, Integer> taxiIds = new HashMap<>();

    public AbstractTripsReader(String delim, DateTimeFormatter format) {
        super(delim, format);
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
                return new TaxiTrip(tripId, Integer.toString(taxiId), //
                        getStartTime(line), //
                        getEndTime(line), //
                        getPickupLocation(line), //
                        getDropoffLocation(line), //
                        getDuration(line), //
                        getDistance(line), //
                        getWaitingTime(line));
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
