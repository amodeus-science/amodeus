/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashSet;
import java.util.Set;

import ch.ethz.idsc.amodeus.net.SimulationObject;

public class RequestRobotaxiInformationElement implements AnalysisElement {
    private final Set<Integer> requestIndices = new HashSet<>();
    private final Set<Integer> vehicleIndices = new HashSet<>();

    @Override
    public void register(SimulationObject simulationObject) {
        simulationObject.requests.stream().forEach(r -> requestIndices.add(r.requestIndex));
        simulationObject.vehicles.stream().forEach(v -> vehicleIndices.add(v.vehicleIndex));
    }

    public int vehicleSize() {
        return vehicleIndices.size();
    }

    public int reqsize() {
        return requestIndices.size();
    }

}
