/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.matsim.xml;

import java.io.File;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

public enum XmlDispatchPeriodChanger {
    ;

    public static void of(File simFolder, int dispatch) throws Exception {
        Config config = ConfigUtils.loadConfig(new File(simFolder, "config_full.xml").toString(), new AmodeusConfigGroup());
        AmodeusConfigGroup.get(config).getModes().values().iterator().next().getDispatcherConfig().getParams().put("dispatchPeriod", String.valueOf(dispatch));
        new ConfigWriter(config).write(new File(simFolder, "config_full.xml").toString());
    }
}
