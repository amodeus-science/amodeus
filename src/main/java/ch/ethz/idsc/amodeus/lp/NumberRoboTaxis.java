package ch.ethz.idsc.amodeus.lp;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVConfigReader;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

/* package */ enum NumberRoboTaxis {
    ;
    
    public static Integer load() {
        Integer numRt = null;
        try{
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(workingDirectory, scenarioOptions.getSimulationConfigName());
        AVConfigGroup avCg = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.toString(), avCg);
        AVConfig avC = provideAVConfig(config, avCg);
        AVGeneratorConfig genConfig = avC.getOperatorConfigs().iterator().next().getGeneratorConfig();
        numRt = (int) genConfig.getNumberOfVehicles();
        }catch(Exception ex){
            System.err.println("cannot load number of RoboTaxis for LPs ");
            ex.printStackTrace();   
        }
        return numRt;
    }

    private static AVConfig provideAVConfig(Config config, AVConfigGroup configGroup) {
        URL configPath = configGroup.getConfigURL();

        if (configPath == null) {
            configPath = ConfigGroup.getInputFileURL(config.getContext(), configGroup.getConfigPath());
        }

        AVConfig avConfig = new AVConfig();
        AVConfigReader reader = new AVConfigReader(avConfig);

        reader.readFile(configPath.getPath());
        return avConfig;
    }


}
