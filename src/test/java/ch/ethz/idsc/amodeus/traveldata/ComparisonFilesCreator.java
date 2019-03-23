package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.virtualnetwork.SaveLoadTest;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkGet;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIO;

public enum ComparisonFilesCreator {
    ;

    public static void main(String[] args) throws Exception {

        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        TestPreparer testPreparer = TestPreparer.run(workingDirectory);
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        VirtualNetwork<Link> vNCreated = VirtualNetworkGet.readDefault(testPreparer.getPreparedNetwork(), scenarioOptions);
        /** create virtual network */
        VirtualNetworkIO.toByte(SaveLoadTest.COMPARISON_VN_FILE, vNCreated);
        /** create travel data */
        TravelData tDCreated = TravelDataGet.readStatic(vNCreated, scenarioOptions);
        TravelDataIO.writeStatic(TravelDataTestHelper.COMPARISON_FILE_TD, (StaticTravelData) tDCreated);

    }
}
