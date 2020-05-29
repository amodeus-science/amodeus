/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.core.controler.AbstractModule;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusVirtualNetworkModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusVirtualNetworkModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        bind(ScenarioOptions.class).toInstance(scenarioOptions);

        for (AmodeusModeConfig modeConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            install(new AmodeusVirtualNetworkModeModule(modeConfig));
        }
    }
}
