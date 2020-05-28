/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

public enum ConfigVehiclesChanger {
    ;

    /** Changes the number of vehicles in the {@link Config} at @param configPath
     * to the value @param vehicleNumber */
    public static void change(String configPath, int vehicleNumber) {
        File configFile = new File(configPath);
        Config config = ConfigUtils.loadConfig(configFile.toString(), new AmodeusConfigGroup());
        AmodeusConfigGroup.get(config).getModes().values().iterator().next()//
                .getGeneratorConfig().setNumberOfVehicles(vehicleNumber);
        new ConfigWriter(config).write(configFile.toString());
    }

}
