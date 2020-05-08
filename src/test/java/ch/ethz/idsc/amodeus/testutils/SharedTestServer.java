/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;

import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import ch.ethz.matsim.av.config.AVConfigGroup;

public class SharedTestServer extends TestServer {

    public SharedTestServer(File workingDirectory) throws Exception {
        super(workingDirectory);
    }

    @Override
    public void simulate() throws Exception {
        // change dispatcher
        Config config = ConfigUtils.loadConfig(scenarioOptions.getSimulationConfigName(), new DvrpConfigGroup(), new AVConfigGroup());
        AVConfigGroup.getOrCreate(config).getOperatorConfigs().values()//
                .iterator().next().getDispatcherConfig().setType("TShareDispatcher");
        new ConfigWriter(config).write(scenarioOptions.getSimulationConfigName());
        super.simulate();
    }
}
