/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

import ch.ethz.matsim.av.config.AVConfigGroup;

public enum XmlGeneratorChanger {
    ;

    /** Changes generator in "av.xml" file in @param simFolder to the value @param newGenerator
     * 
     * @throws Exception */
    public static void of(File simFolder, String newValue) throws Exception {
        Config config = ConfigUtils.loadConfig(new File(simFolder, "config_full.xml").toString(), new AVConfigGroup());
        AVConfigGroup.getOrCreate(config).getOperatorConfigs().values().iterator().next().getGeneratorConfig().setType(newValue);
        new ConfigWriter(config).write(new File(simFolder, "config_full.xml").toString());
    }
}
