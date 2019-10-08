/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

public class LSDataTravelTime implements TravelTime {

    private final LinkSpeedDataContainer lsData;
    private final MatsimAmodeusDatabase db;

    // TODO see if can be converted into 1 class together with
    // ch.ethz.idsc.amodeus.linkspeed.AmodeusLinkSpeedCalculator
    public LSDataTravelTime(LinkSpeedDataContainer lsData, MatsimAmodeusDatabase db) {
        this.lsData = Objects.requireNonNull(lsData);
        this.db = Objects.requireNonNull(db);
    }

    @Override
    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
        Integer linkID = LinkIndex.fromLink(db, link);
        double speed = link.getFreespeed();
        LinkSpeedTimeSeries timeSeries = lsData.getLinkSet().get(linkID);
        if (Objects.nonNull(timeSeries)) {
            LinkSpeedTimeSeries series = lsData.getLinkSet().get(linkID);
            speed = series.getSpeedsFloor((int) time);
        }
        return link.getLength() / speed;
    }

}
