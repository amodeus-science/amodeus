/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.parking.ParkingCapacityAmodeus;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;

public enum ParkingStrategies {
    NONE() {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingStrategy() {
                @Override
                public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, long now) {
                    return new HashMap<>();
                }

                @Override
                public void setRunntimeParameters(ParkingCapacityAmodeus avSpatialCapacityAmodeus, Network network, DistanceFunction distanceFunction) {
                    // ---
                }
            };
        }
    }, //
    RANDOMDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingRandomDiffusion();
        }
    }, //
    ADVANCEDDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingAdvancedDiffusion();
        }
    }, //
    LP {
        @Override
        public ParkingStrategy generateParkingStrategy() {
            return new ParkingLP();
        }
    };

    public abstract ParkingStrategy generateParkingStrategy();

}
