package org.matsim.amodeus.drt;

import java.util.Collection;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.framework.AmodeusBaseModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

import amodeus.amodeus.options.ScenarioOptions;

public class AmodeusDrtModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusDrtModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    public AmodeusDrtModule() {
        this(null);
    }

    @Override
    public void install() {
        install(new AmodeusBaseModule(scenarioOptions));

        AmodeusConfigGroup config = AmodeusConfigGroup.get(getConfig());

        for (AmodeusModeConfig modeConfig : config.getModes().values()) {
            requireDrtMode(getConfig(), modeConfig.getMode());
            install(new AmodeusDrtModeModule(modeConfig));
        }
    }

    static public void overrideDispatchers(Controler controller, Config config) {
        AmodeusConfigGroup amodeusConfig = AmodeusConfigGroup.get(config);

        for (AmodeusModeConfig modeConfig : amodeusConfig.getModes().values()) {
            requireDrtMode(config, modeConfig.getMode());
            controller.addOverridingQSimModule(new AmodeusDrtQSimModeModule(modeConfig.getMode()));
        }
    }

    static private void requireDrtMode(Config config, String mode) {
        Collection<DrtConfigGroup> drtConfigs = MultiModeDrtConfigGroup.get(config).getModalElements();

        for (DrtConfigGroup candidate : drtConfigs) {
            if (candidate.getMode().equals(mode)) {
                return;
            }
        }

        throw new IllegalStateException("Mode '" + mode + "' is defined in Amodeus, but not in DRT!");
    }
}
