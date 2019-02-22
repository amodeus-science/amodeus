package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.Date;
import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

public class TripDateFilter implements DataFilter<Trip> {
    private final Date date;

    public TripDateFilter(Date date) {
        this.date = date;
    }

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        return stream.filter(trip -> //
                StaticHelper.sameDay(date, trip.PickupDate) && StaticHelper.sameDay(date, trip.DropoffDate));
    }

}
