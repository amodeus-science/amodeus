/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TripsReaderChicago extends ChicagoTripsReaderBasic {

    public TripsReaderChicago() {
        super(",", DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a"));
    }

    @Override
    public LocalDateTime getStartTime(String[] line) throws ParseException {
        return LocalDateTime.parse(get(line, "Trip Start Timestamp"), format);
    }

    @Override
    public LocalDateTime getEndTime(String[] line) throws ParseException {
        return LocalDateTime.parse(get(line, "Trip End Timestamp"), format);
    }

    @Override
    public Coord getPickupLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "Pickup Centroid Longitude")), //
                Double.valueOf(get(line, "Pickup Centroid Latitude")));
    }

    @Override
    public Coord getDropoffLocation(String[] line) {
        return new Coord(Double.valueOf(get(line, "Dropoff Centroid Longitude")), //
                Double.valueOf(get(line, "Dropoff Centroid Latitude")));
    }

    @Override
    public Scalar getDuration(String[] line) {
        return Quantity.of(Long.valueOf(get(line, "Trip Seconds")), SI.SECOND);
    }

}
