package ch.ethz.matsim.av.waiting_time;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.WaitingTimeConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class WaitingTimeModule extends AbstractDvrpModeModule {
    public WaitingTimeModule(String mode) {
        super(mode);
    }

    @Override
    public void install() {
        // TODO: Make this more modular?
        bind(StandardWaitingTimeFactory.class);
        bind(WaitingTimeFactory.class).to(StandardWaitingTimeFactory.class);
    }

    static public Collection<Id<AVOperator>> getDynamicOperators(AVConfigGroup config) {
        Set<Id<AVOperator>> dynamicIds = new HashSet<>();

        for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
            WaitingTimeConfig waitingConfig = operatorConfig.getWaitingTimeConfig();

            if (waitingConfig.getEstimationAlpha() > 0.0) {
                dynamicIds.add(operatorConfig.getId());
            }
        }

        return dynamicIds;
    }
}
