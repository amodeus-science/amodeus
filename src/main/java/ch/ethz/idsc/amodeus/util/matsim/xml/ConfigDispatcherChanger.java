/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.matsim.xml;

import java.io.File;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

public enum ConfigDispatcherChanger {
    ;

    /** Changes the dispatcher in the {@link Config} at @param configPath to the
     * new value @param newDipsatcher */
    public static void change(String configPath, String newDipsatcher) {
        File configFile = new File(configPath);
        Config config = ConfigUtils.loadConfig(configFile.toString(), new AmodeusConfigGroup());
        AmodeusConfigGroup.get(config).getModes().values().iterator().next()//
                .getDispatcherConfig().setType(newDipsatcher);
        new ConfigWriter(config).write(configFile.toString());
    }

}
