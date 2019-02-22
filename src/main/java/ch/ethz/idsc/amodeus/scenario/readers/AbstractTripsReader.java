package ch.ethz.idsc.amodeus.scenario.readers;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.scenario.trips.Trip;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractTripsReader extends CsvReader {
    private Map<String, Integer> taxiIds = new HashMap<>();

    /* package */ AbstractTripsReader(String delim, SimpleDateFormat format) {
        super(delim, format);
    }

    public Stream<Trip> getTripStream(File file) throws IOException {
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
                return new Trip(tripId, taxiId, //
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

    abstract String getTaxiCode(String[] line);

    abstract Date getStartTime(String[] line) throws ParseException;

    abstract Date getEndTime(String[] line) throws ParseException;

    abstract Coord getPickupLocation(String[] line);

    abstract Coord getDropoffLocation(String[] line);

    abstract long getDuration(String[] line);

    abstract Double getDistance(String[] line);

    abstract Double getWaitingTime(String[] line);
}
