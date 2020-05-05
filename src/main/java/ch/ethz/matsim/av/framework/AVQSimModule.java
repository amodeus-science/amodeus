package ch.ethz.matsim.av.framework;

import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;

public class AVQSimModule extends AbstractQSimModule {
    static public void configureComponents(QSimComponentsConfig components) {
        // TOOD: Refactor when add modes
        DvrpQSimComponents.activateModes("av").configure(components);
    }

    @Override
    protected void configureQSim() {
        for (OperatorConfig operatorConfig : AVConfigGroup.getOrCreate(getConfig()).getOperatorConfigs().values()) {
            install(new AVQSimModeModule("av"));
        }
    }
}
