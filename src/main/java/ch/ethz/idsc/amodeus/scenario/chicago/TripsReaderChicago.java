/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.scenario.readers.CsvReader;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TripsReaderChicago extends ChicagoTripsReaderBasic {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a");

    public TripsReaderChicago() {
        super(",");

    }

    @Override
    public LocalDateTime getStartTime(CsvReader.Row line) throws ParseException {
        return LocalDateTime.parse(line.get("Trip Start Timestamp"), format);
    }

    @Override
    public LocalDateTime getEndTime(CsvReader.Row line) throws ParseException {
        return LocalDateTime.parse(line.get("Trip End Timestamp"), format);
    }

    @Override
    public Coord getPickupLocation(CsvReader.Row line) {
        return new Coord(Double.valueOf(line.get("Pickup Centroid Longitude")), //
                Double.valueOf(line.get("Pickup Centroid Latitude")));
    }

    @Override
    public Coord getDropoffLocation(CsvReader.Row line) {
        return new Coord(Double.valueOf(line.get("Dropoff Centroid Longitude")), //
                Double.valueOf(line.get("Dropoff Centroid Latitude")));
    }

    @Override
    public Scalar getDuration(CsvReader.Row line) {
        return Quantity.of(Long.valueOf(line.get("Trip Seconds")), SI.SECOND);
    }

}
