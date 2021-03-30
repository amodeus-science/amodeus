/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.traveldata;

import java.io.File;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.sca.Chop;

public class TravelDataTestHelper {
    public static final File COMPARISON_FILE_TD = new File("resources/testComparisonFiles/travelData");

    public static TravelDataTestHelper prepare(VirtualNetwork<Link> vNCreated, VirtualNetwork<Link> vNSaved, ScenarioOptions scenarioOptions) throws Exception {
        return new TravelDataTestHelper(vNCreated, vNSaved, scenarioOptions);
    }

    // ---
    private final TravelData tDCreated;
    private final TravelData tDSaved;

    private TravelDataTestHelper(VirtualNetwork<Link> vNCreated, VirtualNetwork<Link> vNSaved, ScenarioOptions scenarioOptions) throws Exception {
        tDCreated = TravelDataGet.readStatic(vNCreated, scenarioOptions);
        tDSaved = TravelDataIO.readStatic(COMPARISON_FILE_TD, vNSaved);
    }

    public boolean timeIntervalCheck() {
        return tDSaved.getTimeIntervalLength() == tDCreated.getTimeIntervalLength();
    }

    public boolean timeStepsCheck() {
        return tDSaved.getTimeSteps() == tDCreated.getTimeSteps();
    }

    public boolean lambdaAbsoluteCheck() {
        return Chop._06.isClose(tDSaved.getLambdaAbsolute(), tDCreated.getLambdaAbsolute());
    }

    public boolean lambdaAbsoluteAtTimeCheck() {
        return Chop._06.isClose(tDSaved.getLambdaAbsoluteAtTime(1000), tDCreated.getLambdaAbsoluteAtTime(1000));
    }

    public boolean lambdaOutOfRangeCheck() {
        try {
            tDCreated.getLambdaRateAtTime(30 * 3600);
        } catch (Exception exception) {
            return true;
        }
        return false;
    }

    public boolean lambdaRateCheck() {
        return Chop._06.isClose(tDSaved.getLambdaRate(), tDCreated.getLambdaRate());
    }

    public boolean lambdaRateAtTimeCheck() {
        return Chop._06.isClose(tDSaved.getLambdaRateAtTime(1000), tDCreated.getLambdaRateAtTime(1000));
    }

    public boolean lambdaInvalidRateAtTimeCheck() {
        try {
            tDCreated.getLambdaRateAtTime(-1);
        } catch (Exception e) {
            return true;
        }
        return false;
    }
}
