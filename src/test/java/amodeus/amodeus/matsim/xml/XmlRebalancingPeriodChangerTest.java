/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.matsim.xml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import amodeus.amodeus.util.io.CopyFiles;
import amodeus.amodeus.util.io.Locate;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.matsim.xml.ConfigDispatcherChanger;
import amodeus.amodeus.util.matsim.xml.ConfigVehiclesChanger;
import amodeus.amodeus.util.matsim.xml.XmlDispatchPeriodChanger;
import amodeus.amodeus.util.matsim.xml.XmlDistanceHeuristicChanger;
import amodeus.amodeus.util.matsim.xml.XmlGeneratorChanger;
import amodeus.amodeus.util.matsim.xml.XmlRebalancingPeriodChanger;
import ch.ethz.idsc.tensor.ext.DeleteDirectory;

public class XmlRebalancingPeriodChangerTest {
    private static File workingDirectory;

    @BeforeClass
    public static void prepare() throws Exception {
        /** copy av.xml file from test directory */
        File scenarioDirectory = new File(Locate.repoFolder(XmlRebalancingPeriodChangerTest.class, "amodeus"), "resources/testScenario");

        workingDirectory = new File(scenarioDirectory, "Temp");
        if (!workingDirectory.isDirectory())
            workingDirectory.mkdir();

        GlobalAssert.that(workingDirectory.isDirectory());

        CopyFiles.now(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath(), //
                Arrays.asList("config_full.xml"), true);

        System.out.println("wordir: " + workingDirectory.getAbsolutePath());
        System.out.println("scenarioDirectory: " + scenarioDirectory.getAbsolutePath());
    }

    @Test
    public void test() throws Exception {

        String configFilePath = new File(workingDirectory, "config_full.xml").getAbsolutePath();

        /** perform some action on it */
        ConfigVehiclesChanger.change(configFilePath, 111);
        XmlRebalancingPeriodChanger.of(workingDirectory, 222);
        XmlDispatchPeriodChanger.of(workingDirectory, 333);
        XmlGeneratorChanger.of(workingDirectory, "Tannhaeuser");
        ConfigDispatcherChanger.change(configFilePath, "FliegenderHollaender");
        XmlDistanceHeuristicChanger.of(workingDirectory, "Lohegrin22");

        // TODO @sebhoerl when less lazy, open and inspect av.xml, write some tests..
    }

    @AfterClass
    public static void swipeFloor() throws IOException {
        DeleteDirectory.of(workingDirectory, 1, 2);
    }
}
