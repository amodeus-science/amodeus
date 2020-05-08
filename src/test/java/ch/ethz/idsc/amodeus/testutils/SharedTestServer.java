/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;

import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

import ch.ethz.matsim.av.config.AmodeusConfigGroup;

public class SharedTestServer extends TestServer {

    public static SharedTestServer run(File workingDirectory) throws Exception {
        SharedTestServer testServer = new SharedTestServer(workingDirectory);
        testServer.simulate();
        return testServer;
    }

    private SharedTestServer(File workingDirectory) throws Exception {
        super(workingDirectory);
        Config config = ConfigUtils.loadConfig(scenarioOptions.getSimulationConfigName(), new DvrpConfigGroup(), new AmodeusConfigGroup());
        AmodeusConfigGroup.get(config).getModes().values()//
                .iterator().next().getDispatcherConfig().setType("TShareDispatcher");
        new ConfigWriter(config).write(scenarioOptions.getSimulationConfigName());
    }
}
