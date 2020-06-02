/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.traveldata;

import java.io.File;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.test.TestFileHandling;
import amodeus.amodeus.testutils.TestPreparer;
import amodeus.amodeus.util.io.Locate;
import amodeus.amodeus.util.io.MultiFileTools;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNetworkGet;
import amodeus.amodeus.virtualnetwork.core.VirtualNetworkIO;
import amodeus.amodeus.virtualnetwork.core.VirtualNetworkIOTest;

/* package */ enum ComparisonFilesCreator {
    ;

    /** This file can be used to generate the files resources/testScenario/virtualNetwork and
     * resources/testScenario/travelData
     * 
     * ATTENTION:
     * 
     * IF A TEST FAILS AS THESE FILES ARE NOT CONSISTENT WITH THE GENERATED VIRTUAL NETWORK AND
     * TRAVEL DATA, DO NOT USE THIS FUNCTION TO CREATE NEW FILES BUT INVESTIGATE THE REASON
     * OF FAILURE FIRST OR CONSULT WITH clruch@ethz.ch
     * 
     * @param args
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        File scenarioDirectory = new File(Locate.repoFolder(ComparisonFilesCreator.class, "amodeus"), "resources/testScenario");
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        TestPreparer testPreparer = TestPreparer.run(workingDirectory);

        VirtualNetwork<Link> vNCreated = VirtualNetworkGet.readDefault(testPreparer.getPreparedNetwork(), scenarioOptions);
        /** create virtual network */
        VirtualNetworkIO.toByte(VirtualNetworkIOTest.COMPARISON_VN_FILE, vNCreated);
        /** create travel data */
        StaticTravelData tDCreated = TravelDataGet.readStatic(vNCreated, scenarioOptions);
        TravelDataIO.writeStatic(TravelDataTestHelper.COMPARISON_FILE_TD, tDCreated);

        /** remove generated and unneeded files */
        TestFileHandling.removeGeneratedFiles();
    }
}
