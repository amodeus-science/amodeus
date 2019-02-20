/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.io.File;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.LPOptions;
import ch.ethz.idsc.amodeus.options.LPOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum LPPreparer {
    ;

    /** Solves the LP by the given solver and returns the solver where the LP solution can be taken out if it */
    public static LPSolver run(File workingDirectory, VirtualNetwork<Link> virtualNetwork, //
            Network network, Tensor lambdaAbsolute, int numberOfVehicles, int endTime) throws Exception {

        LPOptions lpOptions = new LPOptions(workingDirectory, LPOptionsBase.getDefault());

        LPCreator lpCreator = lpOptions.getLPSolver();
        LPSolver solver = lpCreator.create(virtualNetwork, network, lpOptions, lambdaAbsolute, numberOfVehicles, endTime);

        solver.initiateLP();
        solver.solveLP(false);
        // solver.writeLPSolution();

        return solver;
    }
}
