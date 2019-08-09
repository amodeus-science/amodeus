/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.core.gbl.MatsimRandom;

import com.google.inject.Inject;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.generator.AVGenerator;

/** class generates {@link AVVehicle}s and places them at a random link.
 * each link is equally likely.
 *
 * all vehicles are created in this iteration. after that, no more AVVehiles are
 * added to the system. */
public class RandomDensityGenerator implements AVGenerator {
    private static final Logger LOGGER = Logger.getLogger(RandomDensityGenerator.class);
    // ---
    private final Network network;
    private final int numberOfSeats;
    private final long numberOfVehicles;
    private long generatedVehicles = 0;
    private final String prefix;

    public RandomDensityGenerator(AVGeneratorConfig config, Network network, int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
        numberOfVehicles = config.getNumberOfVehicles();
        String config_prefix = config.getPrefix();
        prefix = config_prefix == null ? "av_" + config.getParent().getId().toString() + "_" : config_prefix + "_";
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public boolean hasNext() {
        return generatedVehicles < numberOfVehicles;
    }

    @Override
    public AVVehicle next() {
        ++generatedVehicles;
        int bound = network.getLinks().size();
        int elemRand = MatsimRandom.getRandom().nextInt(bound);
        Link link = network.getLinks().values().stream().skip(elemRand).findFirst().get();
        LOGGER.info("car placed at link " + link);
        Id<DvrpVehicle> id = Id.create("av_" + prefix + String.valueOf(generatedVehicles), DvrpVehicle.class);
        AVVehicle vehicle = new AVVehicle(id, link, numberOfSeats, 0.0, Double.POSITIVE_INFINITY);
        return vehicle;
    }

    public static class Factory implements AVGenerator.AVGeneratorFactory {
        @Inject
        private Network network;

        @Override
        public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
            int numberOfSeats = Integer.parseInt(generatorConfig.getParams().getOrDefault("numberOfSeats", "4"));
            return new RandomDensityGenerator(generatorConfig, network, numberOfSeats);
        }
    }
}
