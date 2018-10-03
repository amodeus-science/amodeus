/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.Serializable;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.element.RequestRobotaxiInformationElement;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;

public class AnalysisSummary implements Serializable {

    private final ScenarioParameters scenarioParameters = new ScenarioParameters();
    private final RequestRobotaxiInformationElement simulationInformationElement = new RequestRobotaxiInformationElement();
    private final StatusDistributionElement statusDistribution = new StatusDistributionElement();
    private final DistanceElement distanceElement;
    private final TravelTimeAnalysis travelTimeAnalysis = new TravelTimeAnalysis();;

    /* package */ AnalysisSummary(int numVehicles, int size) {
        distanceElement = new DistanceElement(numVehicles, size);
        /** Standard Least Path Calculator for Standard Travel Times */
    }

    public ScenarioParameters getScenarioParameters() {
        return scenarioParameters;
    }

    public RequestRobotaxiInformationElement getSimulationInformationElement() {
        return simulationInformationElement;
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

}
