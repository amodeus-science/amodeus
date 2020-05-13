/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class LSDataTravelTime implements TravelTime {
    private final LinkSpeedDataContainer lsData;

    // TODO @clruch see if can be converted into 1 class together with
    // ch.ethz.idsc.amodeus.linkspeed.AmodeusLinkSpeedCalculator
    public LSDataTravelTime(LinkSpeedDataContainer lsData) {
        this.lsData = Objects.requireNonNull(lsData);
    }

    @Override
    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
        double speed = link.getFreespeed();
        LinkSpeedTimeSeries timeSeries = lsData.get(link);
        if (Objects.nonNull(timeSeries))
            speed = timeSeries.getSpeedsFloor((int) time);
        return link.getLength() / speed;
    }
}
