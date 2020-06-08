/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import amodeus.amodeus.analysis.element.DistanceElement;
import amodeus.amodeus.analysis.element.NumberPassengersAnalysis;
import amodeus.amodeus.analysis.element.RequestRobotaxiInformationElement;
import amodeus.amodeus.analysis.element.StatusDistributionElement;
import amodeus.amodeus.analysis.element.TravelTimeAnalysis;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.options.ScenarioOptions;

/** Analysis summary, builds and contains various default analysis elements */
public class AnalysisSummary implements Serializable {

    private final ScenarioParameters scenarioParameters;
    private final RequestRobotaxiInformationElement reqInfoElement;
    private final StatusDistributionElement statusDistribution = new StatusDistributionElement();
    private final DistanceElement distanceElement;
    private final TravelTimeAnalysis travelTimeAnalysis = new TravelTimeAnalysis();
    private final NumberPassengersAnalysis numberPassengersAnalysis;

    /** @param vehicleIndices - indices of all vehicles in the fleet
     * @param db - amodeus database, default analysis elements will be computed based on the snapshot information therein
     * @param scenarioOptions - scenario options */
    public AnalysisSummary(Set<Integer> vehicleIndices, MatsimAmodeusDatabase db, ScenarioOptions scenarioOptions) {
        Objects.requireNonNull(db);
        reqInfoElement = new RequestRobotaxiInformationElement();
        distanceElement = new DistanceElement(vehicleIndices, db, reqInfoElement);
        scenarioParameters = new ScenarioParameters(scenarioOptions);
        numberPassengersAnalysis = new NumberPassengersAnalysis(vehicleIndices);
    }

    public ScenarioParameters getScenarioParameters() {
        return scenarioParameters;
    }

    public RequestRobotaxiInformationElement getSimulationInformationElement() {
        return reqInfoElement;
    }

    public StatusDistributionElement getStatusDistribution() {
        return statusDistribution;
    }

    public DistanceElement getDistanceElement() {
        return distanceElement;
    }

    public TravelTimeAnalysis getTravelTimeAnalysis() {
        return travelTimeAnalysis;
    }

    public NumberPassengersAnalysis getNumberPassengersAnalysis() {
        return numberPassengersAnalysis;
    }
}
