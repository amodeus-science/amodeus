/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.gbl.MatsimRandom;

import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.generator.AmodeusIdentifiers;

/** class generates {@link AVVehicle}s and places them at a random link.
 * each link is equally likely.
 *
 * all vehicles are created in this iteration. after that, no more AVVehiles are
 * added to the system. */
public class RandomDensityGenerator implements AVGenerator {
    private static final Logger LOGGER = Logger.getLogger(RandomDensityGenerator.class);
    // ---
    private final Network network;
    private final int capacity;
    private final AmodeusModeConfig operatorConfig;

    public RandomDensityGenerator(AmodeusModeConfig operatorConfig, Network network, int capacity) {
        this.network = Objects.requireNonNull(network);
        this.operatorConfig = operatorConfig;
        this.capacity = capacity;
    }

    @Override
    public List<DvrpVehicleSpecification> generateVehicles() {
        long generatedVehicles = 0;
        List<DvrpVehicleSpecification> vehicles = new LinkedList<>();
        while (generatedVehicles < operatorConfig.getGeneratorConfig().getNumberOfVehicles()) {
            ++generatedVehicles;

            int bound = network.getLinks().size();
            int elemRand = MatsimRandom.getRandom().nextInt(bound);
            Link link = network.getLinks().values().stream().skip(elemRand).findFirst().get();
            LOGGER.info("car placed at link " + link);

            Id<DvrpVehicle> id = AmodeusIdentifiers.createVehicleId(operatorConfig.getMode(), generatedVehicles);
            vehicles.add(ImmutableDvrpVehicleSpecification.newBuilder() //
                    .id(id) //
                    .serviceBeginTime(0.0) //
                    .serviceEndTime(Double.POSITIVE_INFINITY) //
                    .capacity(capacity) //
                    .startLinkId(link.getId()) //
                    .build());
        }
        return vehicles;
    }

    public static class Factory implements AVGenerator.AVGeneratorFactory {
        @Override
        public AVGenerator createGenerator(InstanceGetter inject) {
            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            int capacity = operatorConfig.getGeneratorConfig().getCapacity();

            return new RandomDensityGenerator(operatorConfig, network, capacity);
        }
    }
}
