/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import com.google.common.base.Function;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ class ParkingDirectedDiffusion extends AbstractParkingStrategy {

    private final Random random;

    public ParkingDirectedDiffusion(Random random) {
        this.random = random;
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRoboTaxis, //
            Collection<RoboTaxi> rebTaxis, long now) {

        
        
        Map<Link, Set<RoboTaxi>> stayTaxis = StaticHelper.getOccupiedLinks(stayingRoboTaxis);

        DirectedDiffusionHelper helper = //
                new DirectedDiffusionHelper(parkingCapacity, stayingRoboTaxis, rebTaxis, random);

        Map<RoboTaxi, Link> directives = new HashMap<>();
        /** create directed diffusion directives */
        stayTaxis.entrySet().stream().forEach(e -> {
            Link link = e.getKey();
            Set<RoboTaxi> taxis = e.getValue();
            long capacity = parkingCapacity.getSpatialCapacity(link.getId());
            if (taxis.size() > capacity) {
                taxis.stream().limit(taxis.size() - capacity)//
                        .forEach(rt -> {
                            directives.put(rt, helper.getDestinationLink(rt));
                        });
            }
        });
        return directives;
    }

}
