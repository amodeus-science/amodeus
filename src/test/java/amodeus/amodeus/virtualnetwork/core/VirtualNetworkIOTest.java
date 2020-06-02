/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.virtualnetwork.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.test.TestFileHandling;
import amodeus.amodeus.testutils.TestPreparer;
import amodeus.amodeus.traveldata.TravelDataTestHelper;
import amodeus.amodeus.util.io.Locate;
import amodeus.amodeus.util.io.MultiFileTools;

public class VirtualNetworkIOTest {
    public static final File COMPARISON_VN_FILE = new File("resources/testComparisonFiles/virtualNetwork");

    private static TravelDataTestHelper travelDataTestHelper;
    private static TestPreparer testPreparer;
    private static ScenarioOptions scenarioOptions;
    private static VirtualNetwork<Link> vNCreated;
    private static VirtualNetwork<Link> vNSaved;

    @BeforeClass
    public static void before() throws Exception {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        File scenarioDirectory = new File(Locate.repoFolder(VirtualNetworkIOTest.class, "amodeus"), "resources/testScenario");
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());
        scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        testPreparer = TestPreparer.run(workingDirectory);
        vNCreated = VirtualNetworkGet.readDefault(testPreparer.getPreparedNetwork(), scenarioOptions);
        Map<String, Link> map = new HashMap<>();
        testPreparer.getPreparedNetwork().getLinks().forEach((id, link) -> map.put(id.toString(), link));
        vNSaved = VirtualNetworkIO.fromByte(map, COMPARISON_VN_FILE);
        travelDataTestHelper = TravelDataTestHelper.prepare(vNCreated, vNSaved, scenarioOptions);
    }

    @Test
    public void test() throws Exception {
        // consistency of virtualNetwork
        assertEquals(scenarioOptions.getNumVirtualNodes(), vNCreated.getVirtualNodes().size());
        assertEquals(vNSaved.getVirtualNodes().size(), vNCreated.getVirtualNodes().size());
        assertEquals(vNSaved.getVirtualLinks().size(), vNCreated.getVirtualLinks().size());
        assertEquals(vNSaved.getVirtualLink(0).getId(), vNCreated.getVirtualLink(0).getId());
        assertEquals(vNSaved.getVirtualLink(5).getFrom().getId(), vNCreated.getVirtualLink(5).getFrom().getId());
        assertEquals(vNSaved.getVirtualLink(6).getTo().getId(), vNCreated.getVirtualLink(6).getTo().getId());
        assertEquals(vNSaved.getVirtualNode(0).getLinks().size(), vNCreated.getVirtualNode(0).getLinks().size());
        assertEquals(vNSaved.getVirtualNode(1).getLinks().size(), vNCreated.getVirtualNode(1).getLinks().size());
        assertEquals(vNSaved.getVirtualNode(2).getLinks().size(), vNCreated.getVirtualNode(2).getLinks().size());
        assertEquals(vNSaved.getVirtualNode(3).getLinks().size(), vNCreated.getVirtualNode(3).getLinks().size());

        // consistency of travelData
        assertTrue(travelDataTestHelper.timeIntervalCheck());
        assertTrue(travelDataTestHelper.timeStepsCheck());
        assertTrue(travelDataTestHelper.lambdaAbsoluteCheck());
        assertTrue(travelDataTestHelper.lambdaAbsoluteAtTimeCheck());
        assertTrue(travelDataTestHelper.lambdaOutOfRangeCheck());
        assertTrue(travelDataTestHelper.lambdaRateCheck());
        assertTrue(travelDataTestHelper.lambdaRateAtTimeCheck());
        assertTrue(travelDataTestHelper.lambdaInvalidRateAtTimeCheck());
    }

    @AfterClass
    public static void cleanUp() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
