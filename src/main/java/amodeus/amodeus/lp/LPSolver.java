/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.lp;

import ch.ethz.idsc.tensor.Tensor;

public interface LPSolver {
    /** Initiates the LP problem in GLPK
     * 
     * @param lambda is {@link Tensor} that contains the customer requests distributed in virtual stations and time intervals
     * @param numberOfVehicles is the number of available vehicles. */
    void initiateLP();

    /** Executes the LP with a boolean to turn the terminal output on/off */
    void solveLP(boolean mute);

    /** Returns the rebalancing rates */
    Tensor getAlphaRate_ij();

    /** Returns the absolute rebalancing numbers */
    Tensor getAlphaAbsolute_ij();

    /** Returns the customer driving vehicle rates */
    Tensor getFRate_ij();

    /** Returns the customer driving vehicle numbers */
    Tensor getFAbsolute_ij();

    /** Returns the initial vehicles distribution */
    Tensor getV0_i();

    /** Returns the discretized time interval length of the LP */
    int getTimeIntervalLength();

    /** Outputs the LP solution */
    void writeLPSolution();

}
