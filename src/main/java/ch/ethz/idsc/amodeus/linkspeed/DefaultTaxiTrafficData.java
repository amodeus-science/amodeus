/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

@Singleton
/* package */ class DefaultTaxiTrafficData implements TaxiTrafficData {

    private final LinkSpeedDataContainer lsData;
    private final MatsimAmodeusDatabase db;

    public DefaultTaxiTrafficData(LinkSpeedDataContainer lsData, MatsimAmodeusDatabase db) {
        this.db = db;
        this.lsData = Objects.requireNonNull(lsData);
    }

    @Override
    public double getTrafficSpeed(Link link, double now) {
        Integer index = db.getLinkIndex(link);
        LinkSpeedTimeSeries series = lsData.getLinkSet().get(index);
        Double speed = link.getFreespeed();
        if (Objects.nonNull(series)) {
            Double trafficSpeed = series.getSpeedsFloor((int) now);
            if (Objects.nonNull(trafficSpeed))
                speed = trafficSpeed;
        }
        return speed;
    }
}
