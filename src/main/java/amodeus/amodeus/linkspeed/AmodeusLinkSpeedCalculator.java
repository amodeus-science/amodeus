/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

/* package */ final class AmodeusLinkSpeedCalculator implements LinkSpeedCalculator {

    private final TaxiTrafficData taxiTrafficData;

    public AmodeusLinkSpeedCalculator(TaxiTrafficData taxiTrafficData) {
        this.taxiTrafficData = taxiTrafficData;
    }

    @Override
    public double getMaximumVelocity(QVehicle vehicle, Link link, double time) {
        return taxiTrafficData.getTrafficSpeed(link, time);
    }
}
