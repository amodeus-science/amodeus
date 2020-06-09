/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import amodeus.amodeus.net.SimulationObject;

@FunctionalInterface
public interface AnalysisElement {
    /** registers a simulation object at each time step
     * 
     * @param simulationObject */
    void register(SimulationObject simulationObject);

    /** Finishes the analysis after the simulation
     * Output of the results of this Listener */
    default void consolidate() {
        // ---
    }
}
