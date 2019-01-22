/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Dimensions;

/** This solver is called when no LP has to be solved. alpha, f and v0_i are set to 0 matrices with the proper dimensions. */
public class LPEmpty implements LPSolver {
    protected final int timeSteps;
    protected final int timeIntervalLength;
    protected Tensor alphaAbsolute_ij;
    protected Tensor alphaRate_ij;
    protected Tensor fAbsolute_ij;
    protected Tensor fRate_ij;
    protected Tensor v0_i;

    // map with variableIDs in problem set up and linkIDs of virtualNetwork
    protected final Map<List<Integer>, Integer> alphaIDvarID = new HashMap<>();
    protected final Map<List<Integer>, Integer> vIDvarID = new HashMap<>();

    /** @param virtualNetworkIn
     *            the virtual network (complete directed graph) on which the optimization is computed. */
    public LPEmpty(VirtualNetwork<Link> virtualNetwork, Tensor lambdaAbsolute_ij, int endTime) {
        int nvNodes = virtualNetwork.getvNodesCount();
        timeSteps = Dimensions.of(lambdaAbsolute_ij).get(0);
        timeIntervalLength = endTime / timeSteps;
        alphaAbsolute_ij = Array.zeros(timeSteps, nvNodes, nvNodes);
        alphaRate_ij = Array.zeros(timeSteps, nvNodes, nvNodes);
        fAbsolute_ij = Array.zeros(timeSteps, nvNodes, nvNodes);
        fRate_ij = Array.zeros(timeSteps, nvNodes, nvNodes);
        v0_i = Array.zeros(nvNodes);
    }

    /** initiate the linear program */
    @Override
    public final void initiateLP() {
        return;
    }

    @Override
    public void solveLP(boolean mute) {
        return;
    }

    @Override
    public Tensor getAlphaAbsolute_ij() {
        return alphaAbsolute_ij;
    }

    @Override
    public Tensor getAlphaRate_ij() {
        return alphaRate_ij;
    }

    @Override
    public Tensor getFAbsolute_ij() {
        return fAbsolute_ij;
    }

    @Override
    public Tensor getFRate_ij() {
        return fRate_ij;
    }

    @Override
    public Tensor getV0_i() {
        return v0_i;
    }

    /** writes the solution of the LP on the console */
    @Override
    public void writeLPSolution() {
        return;
    }

    @Override
    public int getTimeIntervalLength() {
        return timeIntervalLength;
    }

}