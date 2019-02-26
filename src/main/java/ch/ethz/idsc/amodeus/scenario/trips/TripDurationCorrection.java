/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

/* package */ class TripDurationCorrection implements DataFilter<Trip> {

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        return stream.peek(trip -> {
            long duration = (trip.DropoffDate.getTime() - trip.PickupDate.getTime())/1000;
            if (trip.Duration != duration) {
                System.out.println("correction: trip " + trip.Id + ": duration " + trip.Duration + " -> " + duration);
                trip.Duration = duration;
            }
        });
    }

}
