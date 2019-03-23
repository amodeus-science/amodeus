/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

public class TripDateFilter implements DataFilter<TaxiTrip> {
    private final LocalDateTime date;

    public TripDateFilter(LocalDateTime date) {
        this.date = date;
    }

    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
        return stream.filter(trip -> //
        StaticHelper.sameDay(date, trip.pickupDate) && StaticHelper.sameDay(date, trip.dropoffDate));
    }
}
