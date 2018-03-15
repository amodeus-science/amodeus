/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkGet;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVConfigReader;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVOperatorConfig;

public class ScenarioParameters implements Serializable {
    public final int populationSize;
    public final int iterations;
    public final int redispatchPeriod;
    public final int rebalancingPeriod;

    public final String virtualNodes;
    public final String dispatcher;
    public final String networkName;
    public final String user;
    public final String date;

    public ScenarioParameters() {
        File workingDirectory = null;
        ScenarioOptions scenOptions = null;
        try {
            workingDirectory = new File("").getCanonicalFile();
            scenOptions = ScenarioOptions.load(workingDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File configFile = new File(workingDirectory, scenOptions.getSimulationConfigName());
        Config config = ConfigUtils.loadConfig(configFile.toString());

        user = System.getProperty("user.name");
        date = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss").format(new Date());

        File basePath = new File(config.getContext().getPath()).getParentFile();
        File configPath = new File(basePath, "av.xml");
        AVConfig avConfig = new AVConfig();
        AVConfigReader reader = new AVConfigReader(avConfig);
        reader.readFile(configPath.getAbsolutePath());
        AVOperatorConfig oc = avConfig.getOperatorConfigs().iterator().next();
        AVDispatcherConfig avdispatcherconfig = oc.getDispatcherConfig();
        SafeConfig safeConfig = SafeConfig.wrap(avdispatcherconfig);

        redispatchPeriod = safeConfig.getInteger("dispatchPeriod", -1);
        rebalancingPeriod = safeConfig.getInteger("rebalancingPeriod", -1);
        dispatcher = avdispatcherconfig.getStrategyName();
        Scenario scenario = ScenarioUtils.loadScenario(config);

        populationSize = scenario.getPopulation().getPersons().values().size();

        Network network = scenario.getNetwork();
        if (Objects.isNull(network.getName()))
            networkName = "no network name found in network.xml";
        else
            networkName = network.getName();

        VirtualNetwork<Link> virtualNetwork = null;
        try {
            virtualNetwork = VirtualNetworkGet.readDefault(network);

        } catch (IOException e) {
            System.err.println("INFO not able to load virtual network for report");
            e.printStackTrace();
        }

        if (Objects.isNull(virtualNetwork))
            virtualNodes = "no virtual network found";
        else
            virtualNodes = Integer.toString(virtualNetwork.getvNodesCount()) + " virtual nodes.";

        iterations = config.controler().getLastIteration();

    }

}
