/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.testutils;

import java.io.File;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

public class SharedTestServer extends TestServer {

    public SharedTestServer(File workingDirectory) throws Exception {
        super(workingDirectory);
    }

    @Override
    public void simulate() throws Exception {
        // change dispatcher
        Config config = ConfigUtils.loadConfig(scenarioOptions.getSimulationConfigName(), new DvrpConfigGroup(), new AmodeusConfigGroup());
        AmodeusConfigGroup.get(config).getModes().values()//
                .iterator().next().getDispatcherConfig().setType("TShareDispatcher");
        new ConfigWriter(config).write(scenarioOptions.getSimulationConfigName());
        super.simulate();
    }
}
