/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.net.SimulationObject;

public class RequestRobotaxiInformationElement implements AnalysisElement, TotalValueAppender {
    private final Set<Integer> requestIndices = new HashSet<>();
    private final Set<Integer> vehicleIndices = new HashSet<>();

    @Override
    public void register(SimulationObject simulationObject) {
        simulationObject.requests.forEach(r -> requestIndices.add(r.requestIndex));
        simulationObject.vehicles.forEach(v -> vehicleIndices.add(v.vehicleIndex));
    }

    public int vehicleSize() {
        return vehicleIndices.size();
    }

    public int reqsize() {
        return requestIndices.size();
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> map = new HashMap<>();
        map.put(TtlValIdent.TOTALREQUESTS, String.valueOf(reqsize()));
        map.put(TtlValIdent.TOTALVEHICLES, String.valueOf(vehicleSize()));

        return map;
    }

}
