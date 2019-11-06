/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

import java.io.File;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.NetworkConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

public enum NetworkLoader {
    ;

    public static Network fromNetworkFile(File networkFile) {
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile.getAbsolutePath());
        return network;
    }

    public static Network fromConfigFile(File configFile) {
        Config config = ConfigUtils.loadConfig(configFile.toString());
        config.getModules().keySet().stream().filter(s -> !s.equals(NetworkConfigGroup.GROUP_NAME)) //
                .collect(Collectors.toSet()).forEach(config::removeModule);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        return scenario.getNetwork();
    }
}