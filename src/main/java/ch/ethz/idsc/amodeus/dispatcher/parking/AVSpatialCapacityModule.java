/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

//TODO class not used outside project: document purpose or hide implementation
public class AVSpatialCapacityModule extends AbstractModule {

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public AVSpatialCapacityAmodeus provideAVSpatialCapacity(Network network) {
        try {
            return loadSpatialCapacity(network);
        } catch (IOException ioException) {
            System.err.println("We could not load the Spatial Capacity of all the Links");
            ioException.printStackTrace();
            new RuntimeException();
        }
        return null;
    }

    private static AVSpatialCapacityAmodeus loadSpatialCapacity(Network network) throws IOException {
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        return scenarioOptions.getParkingCapacityGenerator().generate(network);
    }

}
