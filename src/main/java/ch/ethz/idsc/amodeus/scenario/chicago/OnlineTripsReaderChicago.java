/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class OnlineTripsReaderChicago extends ChicagoTripsReaderBasic {

    public OnlineTripsReaderChicago() {
        super(",", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public LocalDateTime getStartTime(String[] line) throws ParseException {
        return LocalDateTime.parse(get(line, "trip_start_timestamp"), format);
    }

    public LocalDateTime getEndTime(String[] line) throws ParseException {
        return LocalDateTime.parse(get(line, "trip_end_timestamp"), format);
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
