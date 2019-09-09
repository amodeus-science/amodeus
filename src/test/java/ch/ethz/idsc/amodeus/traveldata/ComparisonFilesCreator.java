/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkGet;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIO;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIOTest;

public enum ComparisonFilesCreator {
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
        TravelData tDCreated = TravelDataGet.readStatic(vNCreated, scenarioOptions);
        TravelDataIO.writeStatic(TravelDataTestHelper.COMPARISON_FILE_TD, (StaticTravelData) tDCreated);

        /** remove generated and unneeded files */
        TestFileHandling.removeGeneratedFiles();

    }
}
