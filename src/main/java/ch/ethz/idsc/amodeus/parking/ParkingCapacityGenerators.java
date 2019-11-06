/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking;

import java.util.Objects;
import java.util.Random;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityConstant;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityInfinity;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityLinkLength;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityNetworkDistribution;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityNetworkIdentifier;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityUniformRandom;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacityUniformRandomPopulationZone;

public enum ParkingCapacityGenerators implements ParkingCapacityGenerator {
    NONE {
        @Override
        public ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions) {
            return new ParkingCapacityInfinity(network);
        }
    },
    CONSTANT {
        @Override
        public ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions) {
            long capacity = scenarioOptions.getInt(ScenarioOptionsBase.PARKINGCONSTANTCAPACITY);
            return new ParkingCapacityConstant(network, capacity);
        }
    },
    UNIFORMRANDOM {
        @Override
        public ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions) {
            long capacity = scenarioOptions.getInt(ScenarioOptionsBase.PARKINGTOTALSPACES);
            long seed = scenarioOptions.getRandomSeed();
            return new ParkingCapacityUniformRandom(network, population, capacity, new Random(seed));
        }
    },
    UNIFORMRANDOMPOPULATIONZONE {
        @Override
        public ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions) {
            long capacity = scenarioOptions.getInt(ScenarioOptionsBase.PARKINGTOTALSPACES);
            long seed = scenarioOptions.getRandomSeed();
            return new ParkingCapacityUniformRandomPopulationZone(network, population, capacity, new Random(seed));
        }
    },
    LINKDENSITY {
        @Override
        public ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions) {
            double capacityPerLengthUnit = scenarioOptions.getDouble(ScenarioOptionsBase.PARKINGLENGTHDENSITY);
            long minCapacityGlobal = scenarioOptions.getInt(ScenarioOptionsBase.PARKINGMINlINKCAPACITY);
            return new ParkingCapacityLinkLength(network, capacityPerLengthUnit, minCapacityGlobal);
        }
    },
    NETWORKBASED {
        @Override
        public ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions) {
            Objects.requireNonNull(scenarioOptions);
            long seed = scenarioOptions.getRandomSeed();
            return new ParkingCapacityNetworkIdentifier(network, //
                    scenarioOptions.getParkingSpaceTagInNetwork(), new Random(seed));
        }
    },
    NETWORKBASEDRANDOM {
        @Override
        public ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions) {
            long capacity = Objects.requireNonNull(scenarioOptions).getInt(ScenarioOptionsBase.PARKINGTOTALSPACES);
            long seed = scenarioOptions.getRandomSeed();
            return new ParkingCapacityNetworkDistribution(network, scenarioOptions.getParkingSpaceTagInNetwork(), //
                    new Random(seed), capacity);
        }
    };
}