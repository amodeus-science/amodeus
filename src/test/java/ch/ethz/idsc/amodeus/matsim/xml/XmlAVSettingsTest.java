/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class XmlAVSettingsTest {

    @BeforeClass
    public static void prepare() throws Exception {

        /** copy av.xml file from test directory */
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        File scenarioDirectory = new File(LocateUtils.getSuperFolder("amodeus"), "resources/testScenario");
        GlobalAssert.that(workingDirectory.isDirectory());
        CopyFiles.now(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath(), //
                Arrays.asList(new String[] { "av.xml" }), true);

        /** perform some action on it */
        XmlNumberOfVehiclesChanger.of(workingDirectory, 111);
        XmlRebalancingPeriodChanger.of(workingDirectory, 222);
        XmlDispatchPeriodChanger.of(workingDirectory, 333);
        XmlGeneratorChanger.of(workingDirectory, "Tannhaeuser");
        XmlDispatcherChanger.of(workingDirectory, "FliegenderHollaender");
        XmlDistanceHeuristicChanger.of(workingDirectory, "Lohegrin22");

        // TODO when less lazy, open and inspect av.xml, write some tests..

    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

    @AfterClass
    public static void swipeFloor() throws IOException {
        DeleteDirectory.of(new File("av.xml"), 1, 1);
    }

}
