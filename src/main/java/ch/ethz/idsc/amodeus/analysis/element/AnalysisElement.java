/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.amodeus.net.simobj.SimulationObject;

public interface AnalysisElement {
    /** registers a simulation object at each time step
     * 
     * @param simulationObject */
    void register(SimulationObject simulationObject);

    /** Finishes the analysis after the simulation
     * Output of the results of this Listener
     * 
     * @param relativeDirectory
     * @throws Exception */
    default void consolidate() {
        // ---
    }
}
