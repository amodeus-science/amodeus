/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.dispatcher.parking.strategies.ParkingStrategy;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;

/** This Matsim Module is Required for all dispatchers which take Parking into consideration.
 * It provides the Parking Capacities of all the Links and provides as well the strategy to avoid overfilling. */
public class AmodeusParkingModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    /** This Matsim Module is Required for all dispatchers which take Parking into consideration.
     * It provides the Parking Capacities of all the Links and provides as well the strategy to avoid overfilling. */
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
    public ParkingCapacityAmodeus provideAVSpatialCapacity(Network network) {
        try {
            return loadSpatialCapacity(network, scenarioOptions);
        } catch (Exception exception) {
            System.err.println("We could not load the Spatial Capacity of all the Links");
            exception.printStackTrace();
            new RuntimeException();
        }
        return null;
    }

    private static ParkingCapacityAmodeus loadSpatialCapacity(Network network, ScenarioOptions scenarioOptions) {
        return scenarioOptions.getParkingCapacityGenerator().generate(network);
    }

}
