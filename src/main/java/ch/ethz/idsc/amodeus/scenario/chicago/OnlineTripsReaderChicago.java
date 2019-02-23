package ch.ethz.idsc.amodeus.scenario.chicago;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.scenario.readers.AbstractTripsReader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OnlineTripsReaderChicago extends AbstractTripsReader {

    public OnlineTripsReaderChicago() {
        super(",", new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS"));
    }

    public String getTaxiCode(String[] line) {
        return get(line, "taxi_id");
    }

    public Date getStartTime(String[] line) throws ParseException {
        return format.parse(get(line, "trip_start_timestamp"));
    }

    public Date getEndTime(String[] line) throws ParseException {
        return format.parse(get(line, "trip_end_timestamp"));
    }

    public Coord getPickupLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "pickup_centroid_longitude")), //
                Double.valueOf(get(line, "pickup_centroid_latitude")));
    }

    public Coord getDropoffLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "dropoff_centroid_longitude")), //
                Double.valueOf(get(line, "dropoff_centroid_latitude")));
    }

    public long getDuration(String[] line) {
        return Long.valueOf(get(line, "trip_seconds"));
    }

    public Double getDistance(String[] line) {
        return Double.valueOf(get(line, "trip_miles")) * 1609.34; // miles to meters
    }

    public Double getWaitingTime(String[] line) {
        return null;
    }
}
