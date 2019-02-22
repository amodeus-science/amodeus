package ch.ethz.idsc.amodeus.scenario.trips;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import org.matsim.api.core.v01.network.Network;

import java.util.Calendar;
import java.util.stream.Stream;

public class TripStartTimeResampling implements DataFilter<Trip> {
    private Calendar calendar = Calendar.getInstance();
    private final double minuteResolution;

    public TripStartTimeResampling(double minuteResolution) {
        this.minuteResolution = minuteResolution;
    }

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        return stream.peek(trip -> {
            calendar.setTime(trip.PickupDate);
            int offsetSec = RandomVariate.of(UniformDistribution.of(-30 * minuteResolution, 30 * minuteResolution)).number().intValue();
            calendar.add(Calendar.SECOND, offsetSec);
            if (!trip.DropoffDate.equals(calendar.getTime())) {
                System.out.println("resampling: trip " + trip.Id + ": pickup date " + trip.PickupDate + " -> " //
                        + calendar.getTime());
                trip.PickupDate = calendar.getTime(); // TODO respect time boundaries
            }
        });
    }

}
