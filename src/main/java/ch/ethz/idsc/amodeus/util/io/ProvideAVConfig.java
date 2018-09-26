package ch.ethz.idsc.amodeus.util.io;

import java.net.URL;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVConfigReader;
import ch.ethz.matsim.av.framework.AVConfigGroup;

public enum ProvideAVConfig {
    ;

    public static AVConfig with(Config config, AVConfigGroup configGroup) {
        URL configPath = configGroup.getConfigURL();
        if (configPath == null) {
            configPath = ConfigGroup.getInputFileURL(config.getContext(), configGroup.getConfigPath());
        }
        AVConfig avConfig = new AVConfig();
        AVConfigReader reader = new AVConfigReader(avConfig);
        reader.readFile(configPath.getPath());
        return avConfig;
    }

}
