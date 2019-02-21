/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataGet;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkGet;

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusVirtualNetworkModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusVirtualNetworkModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Deprecated
    /** Should not be used in amodeus repository anymore. */
    public AmodeusVirtualNetworkModule() {
        try {
            File workingDirectory = MultiFileTools.getWorkingDirectory();
            this.scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public VirtualNetwork<Link> provideVirtualNetwork(Network network) {
        try {
            return VirtualNetworkGet.readDefault(network, scenarioOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    public TravelData provideTravelData(VirtualNetwork<Link> virtualNetwork) {
        try {
            return TravelDataGet.readStatic(virtualNetwork, scenarioOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
