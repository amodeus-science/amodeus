/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.strategies;

import java.util.Collection;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/* package */ class ParkingDirectedDiffusion extends AbstractParkingDiffusionStrategy {

    private final Random random;
    private DirectedDiffusionHelper helper = null;

    public ParkingDirectedDiffusion(Random random) {
        this.random = random;
    }

    @Override
    protected Link destinationCompute(RoboTaxi roboTaxi) {
        return helper.getDestinationLink(roboTaxi);
    }

    @Override
    protected void update(Collection<RoboTaxi> stayTaxis, Collection<RoboTaxi> rebTaxis, long now) {
        helper = new DirectedDiffusionHelper(parkingCapacity, stayTaxis, rebTaxis, random);
    }

}
