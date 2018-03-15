/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.matsim.av.framework.AVConfigGroup;

public enum ConfigCreator {
    ;

    public static void createSimulationConfigFile(Config fullConfig, ScenarioOptions scenOptions) {

        // change population and network such that converted is loaded
        fullConfig.network().setInputFile(scenOptions.getPreparedNetworkName() + ".xml.gz");
        fullConfig.plans().setInputFile(scenOptions.getPreparedPopulationName() + ".xml.gz");

        AVConfigGroup avConfigGroup = new AVConfigGroup();
        fullConfig.addModule(avConfigGroup);

        // save under correct name
        new ConfigWriter(fullConfig).writeFileV2(scenOptions.getSimulationConfigName());
    }

}
