/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.parking.AVSpatialCapacityAmodeus;

public enum ParkingStrategies {
    NONE() {
        @Override
        public ParkingStrategy generateParkingStrategy(AVSpatialCapacityAmodeus avSpatialCapacityAmodeus) {
            return new ParkingStrategy() {
                @Override
                public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, long now) {
                    return new HashMap<>();
                }
            };
        }
    }, //
    RANDOMDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy(AVSpatialCapacityAmodeus avSpatialCapacityAmodeus) {
            return new ParkingRandomDiffusion(avSpatialCapacityAmodeus);
        }
    }, //
    ADVANCEDDIFUSION {
        @Override
        public ParkingStrategy generateParkingStrategy(AVSpatialCapacityAmodeus avSpatialCapacityAmodeus) {
            return new ParkingAdvancedDiffusion(avSpatialCapacityAmodeus);
        }
    }, //
    LP {
        @Override
        public ParkingStrategy generateParkingStrategy(AVSpatialCapacityAmodeus avSpatialCapacityAmodeus) {
            return new ParkingLP(avSpatialCapacityAmodeus);
        }
    };

    public abstract ParkingStrategy generateParkingStrategy(AVSpatialCapacityAmodeus avSpatialCapacityAmodeus);

}
