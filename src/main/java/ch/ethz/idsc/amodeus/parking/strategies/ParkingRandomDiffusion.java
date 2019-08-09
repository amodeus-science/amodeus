/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ class ParkingRandomDiffusion extends AbstractParkingDiffusionStrategy {

    private final Random random;

    public ParkingRandomDiffusion(Random random) {
        this.random = random;
    }

    @Override
    protected Link destinationCompute(RoboTaxi roboTaxi) {
        Link link = roboTaxi.getDivertableLocation();
        List<Link> links = new ArrayList<>(link.getToNode().getOutLinks().values());
        Collections.shuffle(links, random);
        return links.get(0);
    }

    @Override
    protected void update(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now) {
        // --
    }

}
