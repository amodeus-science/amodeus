/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

@Singleton
/* package */ class DefaultTaxiTrafficData implements TaxiTrafficData {

    private final LinkSpeedDataContainer lsData;
    private final MatsimAmodeusDatabase db;

    public DefaultTaxiTrafficData(LinkSpeedDataContainer lsData, MatsimAmodeusDatabase db) {
        this.db = db;
        this.lsData = Objects.requireNonNull(lsData);
    }

    @Override
    public double getTravelTimeData(Link link, double now) {
        Integer index = db.getLinkIndex(link);
        LinkSpeedTimeSeries series = lsData.getLinkSet().get(index);
        if (Objects.isNull(series))
            return link.getLength() / link.getFreespeed();
        Double speed = series.getSpeedsFloor((int) now);
        GlobalAssert.that(speed >= 0);
        return link.getLength() / speed;
    }
}
