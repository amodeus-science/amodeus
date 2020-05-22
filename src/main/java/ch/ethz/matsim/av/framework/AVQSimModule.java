package ch.ethz.matsim.av.framework;

import java.util.ArrayList;
import java.util.List;

import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfigurator;

import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.config.AmodeusModeConfig;

public class AVQSimModule extends AbstractQSimModule {
    static public QSimComponentsConfigurator activateModes(String... modes) {
        return DvrpQSimComponents.activateModes(modes);
    }

    static public QSimComponentsConfigurator activateModes(AmodeusConfigGroup config) {
        List<String> modes = new ArrayList<>(config.getModes().keySet());
        return activateModes(modes.toArray(new String[modes.size()]));
    }

    @Override
    protected void configureQSim() {
        for (AmodeusModeConfig modeConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            install(new AVQSimModeModule(modeConfig));
        }
    }
}
