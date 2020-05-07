package ch.ethz.matsim.av.config;

import org.matsim.core.config.Config;
import org.matsim.core.config.consistency.ConfigConsistencyChecker;

public class AVConfigConsistencyChecker implements ConfigConsistencyChecker {
    @Override
    public void checkConsistency(Config config) {
        AVConfigGroup configGroup = AVConfigGroup.getOrCreate(config);
        configGroup.getOperatorConfigs();
    }
}
