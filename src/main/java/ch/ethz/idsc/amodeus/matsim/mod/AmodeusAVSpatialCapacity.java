package ch.ethz.idsc.amodeus.matsim.mod;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.dispatcher.parking.AVSpatialCapacityAmodeus;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public class AmodeusAVSpatialCapacity extends AbstractModule {
    private final Network network;
    
    public AmodeusAVSpatialCapacity(Network network) {
        this.network = network;
    }
    
    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public AVSpatialCapacityAmodeus provideMPCsetup() {
        try {
            File workingDirectory = MultiFileTools.getWorkingDirectory();
            ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
            return scenarioOptions.getParkingCapacityGenerator().generate(network);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }


}
