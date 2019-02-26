/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.Calendar;
import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

public class TripEndTimeCorrection implements DataFilter<Trip> {
    private Calendar calendar = Calendar.getInstance();

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        return stream.peek(trip -> {
            calendar.setTime(trip.PickupDate);
            calendar.add(Calendar.SECOND, Math.round(trip.Duration));
            if (!trip.DropoffDate.equals(calendar.getTime())) {
                System.out.println("correction: trip " + trip.Id + ": dropoff date " + trip.DropoffDate + " -> " //
                        + calendar.getTime());
                trip.DropoffDate = calendar.getTime();
            }
        });
    }

}
