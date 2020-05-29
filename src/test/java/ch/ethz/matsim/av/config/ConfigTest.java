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
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.AmodeusScoringConfig;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

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
            AmodeusConfigGroup configGroup = new AmodeusConfigGroup();
            Config config = ConfigUtils.createConfig(configGroup);

            AmodeusModeConfig operator1 = new AmodeusModeConfig("id_abc");
            operator1.getDispatcherConfig().setType("disp_abc");
            operator1.getGeneratorConfig().setType("gen_abc");
            operator1.getGeneratorConfig().setNumberOfVehicles(5);
            operator1.getTimingConfig().setPickupDurationPerPassenger(123.0);
            configGroup.addMode(operator1);

            AmodeusModeConfig operator2 = new AmodeusModeConfig("id_uvw");
            operator2.getDispatcherConfig().setType("disp_uvw");
            operator2.getGeneratorConfig().setType("gen_uvw");
            operator2.getGeneratorConfig().setNumberOfVehicles(15);
            operator2.getTimingConfig().setPickupDurationPerPassenger(789.0);
            configGroup.addMode(operator2);

            operator1.clearScoringParameters();

            AmodeusScoringConfig params1 = new AmodeusScoringConfig();
            params1.setSubpopulation(null);
            params1.setMarginalUtilityOfWaitingTime(1.0);
            operator1.addScoringParameters(params1);

            AmodeusScoringConfig params2 = new AmodeusScoringConfig();
            params2.setSubpopulation("xyz");
            params2.setMarginalUtilityOfWaitingTime(15.0);
            operator1.addScoringParameters(params2);

            new ConfigWriter(config).write("test_output/test_config.xml");
        }

        {
            AmodeusConfigGroup configGroup = new AmodeusConfigGroup();
            ConfigUtils.loadConfig("test_output/test_config.xml", configGroup);

            AmodeusModeConfig operator1 = configGroup.getMode("id_abc");
            Assert.assertEquals("id_abc", operator1.getMode());
            Assert.assertEquals("disp_abc", operator1.getDispatcherConfig().getType());
            Assert.assertEquals("gen_abc", operator1.getGeneratorConfig().getType());
            Assert.assertEquals(5, operator1.getGeneratorConfig().getNumberOfVehicles());
            Assert.assertEquals(123.0, operator1.getTimingConfig().getPickupDurationPerPassenger(), 1e-3);

            AmodeusModeConfig operator2 = configGroup.getMode("id_uvw");
            Assert.assertEquals("id_uvw", operator2.getMode());
            Assert.assertEquals("disp_uvw", operator2.getDispatcherConfig().getType());
            Assert.assertEquals("gen_uvw", operator2.getGeneratorConfig().getType());
            Assert.assertEquals(15, operator2.getGeneratorConfig().getNumberOfVehicles());
            Assert.assertEquals(789.0, operator2.getTimingConfig().getPickupDurationPerPassenger(), 1e-3);

            AmodeusScoringConfig params1 = operator1.getScoringParameters(null);
            Assert.assertEquals(1.0, params1.getMarginalUtilityOfWaitingTime(), 1e-2);

            AmodeusScoringConfig params2 = operator1.getScoringParameters("xyz");
            Assert.assertEquals(15.0, params2.getMarginalUtilityOfWaitingTime(), 1e-2);
        }
    }

    @Test
    public void testDuplication() throws IOException {
        // The way the config was set up previously loading and saving the config file
        // would duplicate operators etc. This test makes sure this does not happen
        // anymore.

        AmodeusConfigGroup configGroup = new AmodeusConfigGroup();
        Config config = ConfigUtils.createConfig(configGroup);

        AmodeusModeConfig operator1 = new AmodeusModeConfig("id_abc");
        operator1.getDispatcherConfig().setType("disp_abc");
        operator1.getGeneratorConfig().setType("gen_abc");
        operator1.getGeneratorConfig().setNumberOfVehicles(5);
        operator1.getTimingConfig().setPickupDurationPerPassenger(123.0);
        configGroup.addMode(operator1);

        new ConfigWriter(config).write("test_output/test_config1.xml");

        Config config2 = ConfigUtils.loadConfig("test_output/test_config1.xml", new AmodeusConfigGroup());
        new ConfigWriter(config2).write("test_output/test_config2.xml");

        Config config3 = ConfigUtils.loadConfig("test_output/test_config2.xml", new AmodeusConfigGroup());
        new ConfigWriter(config3).write("test_output/test_config3.xml");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("test_output/test_config3.xml"))));

        String line = null;

        int numberOfOperators = 0;
        int numberOfDispatchers = 0;
        boolean inAmodeus = false;

        while ((line = reader.readLine()) != null) {
            if (line.contains("<module") && line.contains("amodeus")) {
                inAmodeus = true;
            }

            if (inAmodeus) {
                if (line.contains("</module>")) {
                    inAmodeus = false;
                }

                if (line.contains("parameterset") && line.contains("mode")) {
                    numberOfOperators++;
                }

                if (line.contains("parameterset") && line.contains("dispatcher")) {
                    numberOfDispatchers++;
                }
            }
        }

        reader.close();

        Assert.assertEquals(1, numberOfOperators);
        Assert.assertEquals(1, numberOfDispatchers);
    }
}
