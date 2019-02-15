/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.data.Vehicle;
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
    private final long numberOfVehicles;
    private final String prefix;
    private final Network network;

    private long generatedNumberOfVehicles = 0;

    RandomDensityGenerator(AVGeneratorConfig config, Network networkIn, Population population) {

        numberOfVehicles = config.getNumberOfVehicles();

        String config_prefix = config.getPrefix();
        prefix = config_prefix == null ? "av_" + config.getParent().getId().toString() + "_" : config_prefix + "_";

        network = Objects.requireNonNull(networkIn);
    }

    @Override
    public boolean hasNext() {
        return generatedNumberOfVehicles < numberOfVehicles;
    }

    @Override
    public AVVehicle next() {
        ++generatedNumberOfVehicles;

        int bound = network.getLinks().size();
        int elemRand = MatsimRandom.getRandom().nextInt(bound);
        Link linkGen = network.getLinks().values().stream().skip(elemRand).findFirst().get();

        LOGGER.info("car placed at link " + linkGen);

        Id<Vehicle> id = Id.create("av_" + prefix + String.valueOf(generatedNumberOfVehicles), Vehicle.class);
        // In the future increase flexibility by adding capacity parameter as parameter in av.xml
        AVVehicle vehicle = new AVVehicle(id, linkGen, 4.0, 0.0, Double.POSITIVE_INFINITY);
        return vehicle;
    }

    static public class Factory implements AVGenerator.AVGeneratorFactory {
        @Inject
        private Population population;
        @Inject
        private Network network;

        @Override
        public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
            return new RandomDensityGenerator(generatorConfig, network, population);
        }
    }
}
