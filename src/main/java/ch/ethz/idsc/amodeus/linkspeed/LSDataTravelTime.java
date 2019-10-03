/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.red.Mean;

public class LSDataTravelTime implements TravelTime {

    private final LinkSpeedDataContainer lsData;
    private final MatsimAmodeusDatabase db;

    public LSDataTravelTime(LinkSpeedDataContainer lsData, MatsimAmodeusDatabase db) {
        this.lsData = lsData;
        this.db = db;
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
