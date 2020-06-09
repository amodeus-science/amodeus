/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;

import amodeus.amodeus.options.ScenarioOptions;

public enum ConfigCreator {
    ;
    /** * Function writes a simulation config file after prepared network and prepared population
     * are created, the new config file is identical to @param fullConfig, except that network and
     * population file names are changed, the new names are taken from @param scenOptions */
    public static void createSimulationConfigFile(Config fullConfig, ScenarioOptions scenOptions) {
        /** change network and population name */
        fullConfig.network().setInputFile(scenOptions.getPreparedNetworkName() + ".xml.gz");
        fullConfig.plans().setInputFile(scenOptions.getPreparedPopulationName() + ".xml.gz");
        /** save with correct name */
        new ConfigWriter(fullConfig).writeFileV2(scenOptions.getSimulationConfigName());
    }

}
