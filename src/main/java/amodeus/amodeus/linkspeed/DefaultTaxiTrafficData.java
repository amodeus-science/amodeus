/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import com.google.inject.Singleton;

@Singleton
public class DefaultTaxiTrafficData implements TaxiTrafficData {
    private final LinkSpeedDataContainer lsData;

    public DefaultTaxiTrafficData(LinkSpeedDataContainer lsData) {
        this.lsData = Objects.requireNonNull(lsData);
    }

    @Override
    public double getTrafficSpeed(Link link, double now) {
        LinkSpeedTimeSeries series = lsData.get(link);
        Double speed = link.getFreespeed();
        if (Objects.nonNull(series)) {
            Double trafficSpeed = series.getSpeedsFloor((int) now);
            if (Objects.nonNull(trafficSpeed))
                speed = trafficSpeed;
        }
        return speed;
    }

    @Override
    public LinkSpeedDataContainer getLSData() {
        return lsData;
    }
}
