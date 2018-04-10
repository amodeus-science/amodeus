/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

final class AmodeusLinkSpeedCalculator implements LinkSpeedCalculator {

    final private TaxiTrafficData trafficData;

    public AmodeusLinkSpeedCalculator(TaxiTrafficData trafficData) {
        this.trafficData = trafficData;
    }

    // TODO is this math.min necessary? Hard to debug...
    @Override
    public double getMaximumVelocity(QVehicle vehicle, Link link, double time) {
        // first argument: time [s]
        // second argument: speed [m/s]
        double trafficSpeed = link.getLength()/trafficData.getTravelTimeData(link, time);
        return Math.min(trafficSpeed, link.getFreespeed(time));
    }
}
