/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.scenario.readers.AbstractTripsReader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TripsReaderChicago extends AbstractTripsReader {

    public TripsReaderChicago() {
        super(",", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a"));
    }

    public String getTaxiCode(String[] line) {
        return get(line, "Taxi ID");
    }

    public Date getStartTime(String[] line) throws ParseException {
        return format.parse(get(line, "Trip Start Timestamp"));
    }

    public Date getEndTime(String[] line) throws ParseException {
        return format.parse(get(line, "Trip End Timestamp"));
    }

    public Coord getPickupLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "Pickup Centroid Longitude")), //
                Double.valueOf(get(line, "Pickup Centroid Latitude")));
    }

    public Coord getDropoffLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "Dropoff Centroid Longitude")), //
                Double.valueOf(get(line, "Dropoff Centroid Latitude")));
    }

    public long getDuration(String[] line) {
        return Long.valueOf(get(line, "Trip Seconds"));
    }

    public Double getDistance(String[] line) {
        return Double.valueOf(get(line, "Trip Miles")) * 1609.34; // miles to meters
    }

    public Double getWaitingTime(String[] line) {
        return null;
    }
}
