///* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
//package ch.ethz.idsc.amodeus.scenario.trips;
//
//import java.util.Calendar;
//import java.util.stream.Stream;
//
//import org.matsim.api.core.v01.network.Network;
//
//import ch.ethz.idsc.amodeus.options.ScenarioOptions;
//import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
//
//public class TripEndTimeCorrection implements DataFilter<TaxiTrip> {
//    private Calendar calendar = Calendar.getInstance();
//
//    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
//        return stream.peek(trip -> {
//            calendar.setTime(trip.pickupDate);
//            calendar.add(Calendar.SECOND, Math.round(trip.duration.number().intValue()));
//            if (!trip.dropoffDate.equals(calendar.getTime())) {
//                System.out.println("correction: trip " + trip.localId + ": dropoff date " + trip.dropoffDate + " -> " //
//                        + calendar.getTime());
//                trip.dropoffDate = calendar.getTime();
//            }
//        });
//    }
//}
