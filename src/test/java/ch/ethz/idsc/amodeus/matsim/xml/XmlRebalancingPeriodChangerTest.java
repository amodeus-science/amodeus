/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

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
                Arrays.asList(new String[] { "config_full.xml" }), true);

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

        // TODO when less lazy, open and inspect av.xml, write some tests..

    }

    @AfterClass
    public static void swipeFloor() throws IOException {
        DeleteDirectory.of(workingDirectory, 1, 2);
    }

}
