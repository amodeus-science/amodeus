package ch.ethz.idsc.amodeus.scenario.readers;

import org.matsim.api.core.v01.Coord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TripsReaderChicago extends AbstractTripsReader {

    public TripsReaderChicago() {
        super(",", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a"));
    }

    String getTaxiCode(String[] line) {
        return get(line, "Taxi ID");
    }

    Date getStartTime(String[] line) throws ParseException {
        return format.parse(get(line, "Trip Start Timestamp"));
    }

    Date getEndTime(String[] line) throws ParseException {
        return format.parse(get(line, "Trip End Timestamp"));
    }

    Coord getPickupLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "Pickup Centroid Longitude")), //
                Double.valueOf(get(line, "Pickup Centroid Latitude")));
    }

    Coord getDropoffLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "Dropoff Centroid Longitude")), //
                Double.valueOf(get(line, "Dropoff Centroid Latitude")));
    }

    long getDuration(String[] line) {
        return Long.valueOf(get(line, "Trip Seconds"));
    }

    Double getDistance(String[] line) {
        return Double.valueOf(get(line, "Trip Miles")) * 1609.34; // miles to meters
    }

    Double getWaitingTime(String[] line) {
        return null;
    }
}
