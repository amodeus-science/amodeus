/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum LPPreparer {
    ;
    private static LPCreator lpCreator;
    private static LPSolver solver;

    public static void run(VirtualNetwork<Link> virtualNetwork, //
            Network network, TravelData travelData, //
            ScenarioOptions scenarioOptions) throws Exception {

        lpCreator = scenarioOptions.getLPSolver();
        solver = lpCreator.create(virtualNetwork, network, scenarioOptions, travelData);

        solver.initiateLP();
        solver.solveLP(false);
        // solver.writeLPSolution();

        /** creating rebalancing data with LP */
        RebalanceData rebalanceData = new RebalanceData(solver, scenarioOptions);
        rebalanceData.export(scenarioOptions);
    }

    public static LPSolver getLPSolver() {
        return solver;
    }
}
