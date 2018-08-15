/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum LPPreparer {
    ;

    /** Solves the LP by the given solver and returns the solver where the LP solution can be taken */
    public static LPSolver run(VirtualNetwork<Link> virtualNetwork, //
            Network network, Tensor lambdaAbsolute, //
            ScenarioOptions scenarioOptions) throws Exception {

        LPCreator lpCreator = scenarioOptions.getLPSolver();
        LPSolver solver = lpCreator.create(virtualNetwork, network, scenarioOptions, lambdaAbsolute);

        solver.initiateLP();
        solver.solveLP(false);
        // solver.writeLPSolution();

        return solver;
    }
}
