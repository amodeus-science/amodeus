package ch.ethz.matsim.av.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class ConfigTest {
    @BeforeClass
    public static void doYourOneTimeSetup() {
        new File("test_output").mkdir();
    }

    @AfterClass
    public static void doYourOneTimeTeardown() throws IOException {
        FileUtils.deleteDirectory(new File("test_output"));
    }

    @Test
    public void testReadWrite() {
        {
            AVConfigGroup configGroup = new AVConfigGroup();
            Config config = ConfigUtils.createConfig(configGroup);

            OperatorConfig operator1 = new OperatorConfig();
            operator1.setId(AVOperator.createId("id_abc"));
            operator1.getDispatcherConfig().setType("disp_abc");
            operator1.getGeneratorConfig().setType("gen_abc");
            operator1.getGeneratorConfig().setNumberOfVehicles(5);
            operator1.getTimingConfig().setPickupDurationPerPassenger(123.0);
            configGroup.addOperator(operator1);

            OperatorConfig operator2 = new OperatorConfig();
            operator2.setId(AVOperator.createId("id_uvw"));
            operator2.getDispatcherConfig().setType("disp_uvw");
            operator2.getGeneratorConfig().setType("gen_uvw");
            operator2.getGeneratorConfig().setNumberOfVehicles(15);
            operator2.getTimingConfig().setPickupDurationPerPassenger(789.0);
            configGroup.addOperator(operator2);

            configGroup.clearScoringParameters();

            AVScoringParameterSet params1 = new AVScoringParameterSet();
            params1.setSubpopulation(null);
            params1.setMarginalUtilityOfWaitingTime(1.0);
            configGroup.addScoringParameters(params1);

            AVScoringParameterSet params2 = new AVScoringParameterSet();
            params2.setSubpopulation("xyz");
            params2.setMarginalUtilityOfWaitingTime(15.0);
            configGroup.addScoringParameters(params2);

            new ConfigWriter(config).write("test_output/test_config.xml");
        }

        {
            AVConfigGroup configGroup = new AVConfigGroup();
            ConfigUtils.loadConfig("test_output/test_config.xml", configGroup);

            OperatorConfig operator1 = configGroup.getOperatorConfig(AVOperator.createId("id_abc"));
            Assert.assertEquals(AVOperator.createId("id_abc"), operator1.getId());
            Assert.assertEquals("disp_abc", operator1.getDispatcherConfig().getType());
            Assert.assertEquals("gen_abc", operator1.getGeneratorConfig().getType());
            Assert.assertEquals(5, operator1.getGeneratorConfig().getNumberOfVehicles());
            Assert.assertEquals(123.0, operator1.getTimingConfig().getPickupDurationPerPassenger(), 1e-3);

            OperatorConfig operator2 = configGroup.getOperatorConfig(AVOperator.createId("id_uvw"));
            Assert.assertEquals(AVOperator.createId("id_uvw"), operator2.getId());
            Assert.assertEquals("disp_uvw", operator2.getDispatcherConfig().getType());
            Assert.assertEquals("gen_uvw", operator2.getGeneratorConfig().getType());
            Assert.assertEquals(15, operator2.getGeneratorConfig().getNumberOfVehicles());
            Assert.assertEquals(789.0, operator2.getTimingConfig().getPickupDurationPerPassenger(), 1e-3);

            AVScoringParameterSet params1 = configGroup.getScoringParameters(null);
            Assert.assertEquals(1.0, params1.getMarginalUtilityOfWaitingTime(), 1e-2);

            AVScoringParameterSet params2 = configGroup.getScoringParameters("xyz");
            Assert.assertEquals(15.0, params2.getMarginalUtilityOfWaitingTime(), 1e-2);
        }
    }

    @Test
    public void testDuplication() throws IOException {
        // The way the config was set up previously loading and saving the config file
        // would duplicate operators etc. This test makes sure this does not happen
        // anymore.

        AVConfigGroup configGroup = new AVConfigGroup();
        Config config = ConfigUtils.createConfig(configGroup);

        OperatorConfig operator1 = new OperatorConfig();
        operator1.setId(AVOperator.createId("id_abc"));
        operator1.getDispatcherConfig().setType("disp_abc");
        operator1.getGeneratorConfig().setType("gen_abc");
        operator1.getGeneratorConfig().setNumberOfVehicles(5);
        operator1.getTimingConfig().setPickupDurationPerPassenger(123.0);
        configGroup.addOperator(operator1);

        new ConfigWriter(config).write("test_output/test_config1.xml");

        Config config2 = ConfigUtils.loadConfig("test_output/test_config1.xml", new AVConfigGroup());
        new ConfigWriter(config2).write("test_output/test_config2.xml");

        Config config3 = ConfigUtils.loadConfig("test_output/test_config2.xml", new AVConfigGroup());
        new ConfigWriter(config3).write("test_output/test_config3.xml");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("test_output/test_config3.xml"))));

        String line = null;

        int numberOfOperators = 0;
        int numberOfDispatchers = 0;

        while ((line = reader.readLine()) != null) {
            if (line.contains("parameterset") && line.contains("operator")) {
                numberOfOperators++;
            }

            if (line.contains("parameterset") && line.contains("dispatcher")) {
                numberOfDispatchers++;
            }
        }

        reader.close();

        Assert.assertEquals(1, numberOfOperators);
        Assert.assertEquals(1, numberOfDispatchers);
    }
}
