/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;

/** provides the AMoDeus {@link ScenarioOptions} in case needed in a dispatcher */
public class AmodeusOptionsModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusOptionsModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public ScenarioOptions provideDatabase() {
        return scenarioOptions;
    }

}
