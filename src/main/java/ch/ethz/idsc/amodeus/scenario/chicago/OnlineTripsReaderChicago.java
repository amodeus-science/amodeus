/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class OnlineTripsReaderChicago extends ChicagoTripsReaderBasic {

    public OnlineTripsReaderChicago() {
        super(",", new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS"));
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

    public Scalar getDuration(String[] line) {
        return Quantity.of(Long.valueOf(get(line, "trip_seconds")), "s");
    }

}
