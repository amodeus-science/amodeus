/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TripsReaderChicago extends ChicagoTripsReaderBasic {

    public TripsReaderChicago() {
        super(",", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a"));
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

    public Scalar getDuration(String[] line) {
        return Quantity.of(Long.valueOf(get(line, "Trip Seconds")), "s");
    }

}
