package ch.ethz.idsc.amodeus.mpcsetup;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;


public enum MPCsetupGet {
    ;

    public static MPCsetup buildMPCsetup() throws IOException {
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        MPCsetup mpcSetup = MPCsetupCreators.create(scenarioOptions);
        return mpcSetup;
    }

}
