/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.simulation;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.ProvideAVConfig;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

/** Class which loads and stores the most important Properties used in a Scenario Preparer
 * (such as the three configs(AmodeusOptions, Matsimconfig.xml and
 * Av.xml) and the scenario (network and population)). */
// TODO class is not used at all
public class PreparerProperties {

    /** The Three configs */
    private final ScenarioOptions scenarioOptions;
    private final Config config;
    private final AVConfig avConfig;

    /** the scenario which includes the Population and the Network */
    private final Scenario scenario;

    /** Further Variables */
    private final int numberRoboTaxis;
    private final int endTime;

    public PreparerProperties(File workingDirectory) {

        /** amodeus options */
        try {
            scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        /** MATSim config */
        AVConfigGroup avCg = new AVConfigGroup();
        config = ConfigUtils.loadConfig(scenarioOptions.getPreparerConfigName(), avCg);
        avConfig = ProvideAVConfig.with(config, avCg);
        AVGeneratorConfig genConfig = avConfig.getOperatorConfigs().iterator().next().getGeneratorConfig();
        numberRoboTaxis = (int) genConfig.getNumberOfVehicles();
        endTime = (int) config.qsim().getEndTime();
        scenario = ScenarioUtils.loadScenario(config);
    }

    public ScenarioOptions getScenarioOptions() {
        return scenarioOptions;
    }

    public Config getConfig() {
        return config;
    }

    public AVConfig getAvConfig() {
        return avConfig;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public Network getNetwork() {
        return scenario.getNetwork();
    }

    public Population getPopulation() {
        return scenario.getPopulation();
    }

    public int getEndTime() {
        return endTime;
    }

    public int getNumberRoboTaxis() {
        return numberRoboTaxis;
    }
}
