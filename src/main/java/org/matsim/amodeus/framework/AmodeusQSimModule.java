package org.matsim.amodeus.framework;

import java.util.ArrayList;
import java.util.List;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfigurator;

public class AmodeusQSimModule extends AbstractQSimModule {
    /** Activates all modes that are defined in the Amodeus configuration group. */
    static public QSimComponentsConfigurator activateModes(AmodeusConfigGroup config) {
        List<String> modes = new ArrayList<>(config.getModes().keySet());
        return DvrpQSimComponents.activateModes(modes.toArray(new String[modes.size()]));
    }

    /** Activates all modes that are defined in the Amodeus configuration group. */
    static public QSimComponentsConfigurator activateModes(Config config) {
        return activateModes(AmodeusConfigGroup.get(config));
    }

    @Override
    protected void configureQSim() {
        for (AmodeusModeConfig modeConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            install(new AmodeusModeQSimModule(modeConfig));
        }
    }
}
