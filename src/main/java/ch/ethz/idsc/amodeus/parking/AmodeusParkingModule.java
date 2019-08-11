/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;
import ch.ethz.idsc.amodeus.parking.strategies.ParkingStrategy;

/** This MATSim {@link AbstractModule} is required for all dispatchers which take parking into
 * consideration, i.e., the ones with an additional {@link ParkingStrategy}.
 * It provides the parking capacities of all the {@link Link}s and provides
 * as well the strategy to avoid overfilling. */
public class AmodeusParkingModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusParkingModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public ParkingStrategy provideParkingStrategy() {
        return scenarioOptions.getParkingStrategy();
    }

    @Provides
    @Singleton
    public ParkingCapacity provideAVSpatialCapacity(Network network, Population population) {
        try {
            ParkingCapacityGenerator generator = scenarioOptions.getParkingCapacityGenerator();
            return generator.generate(network, population, scenarioOptions);
        } catch (Exception exception) {
            System.err.println("Unable to load parking capacity for all links, returning null.");
            exception.printStackTrace();
        }
        return null;
    }
}
