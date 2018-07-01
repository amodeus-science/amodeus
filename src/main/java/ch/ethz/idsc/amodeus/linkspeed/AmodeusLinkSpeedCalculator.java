/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

final class AmodeusLinkSpeedCalculator implements LinkSpeedCalculator {

    private final TaxiTrafficData trafficData;

    public AmodeusLinkSpeedCalculator(TaxiTrafficData trafficData) {
        this.trafficData = trafficData;
    }

    @Override
    public double getMaximumVelocity(QVehicle vehicle, Link link, double time) {
        // TODO MISC is this math.min necessary? Hard to debug...
        double denom = trafficData.getTravelTimeData(link, time);
        GlobalAssert.that(denom > 0.0);
        double trafficSpeed = link.getLength() / denom;
        return Math.min(trafficSpeed, link.getFreespeed(time));
    }
}
