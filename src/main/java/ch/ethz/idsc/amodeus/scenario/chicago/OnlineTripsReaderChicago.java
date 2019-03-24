/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.scenario.readers.CsvReader;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class OnlineTripsReaderChicago extends ChicagoTripsReaderBasic {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public OnlineTripsReaderChicago() {
        super(",");
    }

    @Override
    public LocalDateTime getStartTime(CsvReader.Row line) throws ParseException {
        return LocalDateTime.parse(line.get("trip_start_timestamp"), format);
    }

    @Override
    public LocalDateTime getEndTime(CsvReader.Row line) throws ParseException {
        return LocalDateTime.parse(line.get("trip_end_timestamp"), format);
    }

    @Override
    public Coord getPickupLocation(CsvReader.Row line) {
        return new Coord(Double.valueOf(line.get("pickup_centroid_longitude")), //
                Double.valueOf(line.get("pickup_centroid_latitude")));
    }

    @Override
    public Coord getDropoffLocation(CsvReader.Row line) {
        return new Coord(Double.valueOf(line.get("dropoff_centroid_longitude")), //
                Double.valueOf(line.get("dropoff_centroid_latitude")));
    }

    @Override
    public Scalar getDuration(CsvReader.Row line) {
        return Quantity.of(Long.valueOf(line.get("trip_seconds")), SI.SECOND);
    }

    public static void main(String[] args) throws ParseException {
        String dateString = "2018-01-22T22:17:25.123";

        /** old Date stuff */
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
        Date date = format.parse(dateString);
        System.out.println(date);
        /** LocalDateTime */
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime ldt = LocalDateTime.parse(dateString, dtf);// dtf.parse(dateString).;
        System.out.println(ldt);

    }

}
