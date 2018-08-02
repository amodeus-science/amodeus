/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public enum RebalanceDataGet {
    ;

    public static RebalanceData readDefault() throws IOException {
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        final File rebalanceDataFile = new File(scenarioOptions.getVirtualNetworkName(), //
                scenarioOptions.getRebalanceDataName());
        System.out.println("loading travelData from " + rebalanceDataFile.getAbsoluteFile());
        try {
            return RebalanceDataIO.read(rebalanceDataFile);
        } catch (Exception e) {
            System.err.println("cannot load default " + rebalanceDataFile);
            e.printStackTrace();
        }
        return null;
    }

}
