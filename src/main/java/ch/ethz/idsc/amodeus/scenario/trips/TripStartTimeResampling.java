/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;

public class TripStartTimeResampling implements DataFilter<TaxiTrip> {
    private final double minuteResolution;

    public TripStartTimeResampling(double minuteResolution) {
        this.minuteResolution = minuteResolution;
    }

    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
        return stream.peek(trip -> {
            int offsetSec = RandomVariate.of(UniformDistribution.of(-30 * minuteResolution, 30 * minuteResolution)).number().intValue();
            LocalDateTime pickupPrev = trip.pickupDate;
            LocalDateTime pickupModi = LocalDateTime.of(pickupPrev.getYear(), pickupPrev.getMonth(), //
                    pickupPrev.getDayOfMonth(), pickupPrev.getHour(), pickupPrev.getMinute(), pickupPrev.getSecond() + offsetSec);
            trip.pickupDate = pickupModi;

        });
    }

}
