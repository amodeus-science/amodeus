/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

import ch.ethz.matsim.av.config.AVConfigGroup;

public enum XmlNumberOfVehiclesChanger {
    ;

    public static void of(File simFolder, int vehicleNumber) throws Exception {
        Config config = ConfigUtils.loadConfig(new File(simFolder, "config_full.xml").toString(), new AVConfigGroup());
        AVConfigGroup.getOrCreate(config).getOperatorConfigs().values().iterator().next().getGeneratorConfig().setNumberOfVehicles(vehicleNumber);
        new ConfigWriter(config).write(new File(simFolder, "config_full.xml").toString());
    }

}
