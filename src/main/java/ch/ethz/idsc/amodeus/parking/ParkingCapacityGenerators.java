/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityFromNetworkIdentifier;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityInfinity;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum ParkingCapacityGenerators implements ParkingCapacityGenerator {
    NONE {
        @Override
        public ParkingCapacity generate(Network network) {
            return new ParkingCapacityInfinity(network);
        }
    },
    NETWORKBASED {
        @Override
        public ParkingCapacity generate(Network network) {
            GlobalAssert.that(scenarioOptions != null);
            return new ParkingCapacityFromNetworkIdentifier(network, scenarioOptions.getParkingSpaceTagInNetwork());
        }
    };

    // TODO JPH: bad style since assignment of static variable: what would be a better solution?
    protected ScenarioOptions scenarioOptions = null;

    public ParkingCapacityGenerators setScenarioOptions(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
        return this;
    }

}
