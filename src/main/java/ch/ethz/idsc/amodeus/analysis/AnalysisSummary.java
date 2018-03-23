/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.Serializable;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.element.RequestRobotaxiInformationElement;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.element.WaitingTimesElement;

public class AnalysisSummary implements Serializable {

    private final ScenarioParameters scenarioParameters;
    private final RequestRobotaxiInformationElement simulationInformationElement = new RequestRobotaxiInformationElement();
    private final StatusDistributionElement statusDistribution = new StatusDistributionElement();
    private final WaitingTimesElement waitingTimes = new WaitingTimesElement();
    private final DistanceElement distanceElement;

    public AnalysisSummary(int numVehicles, int size, ScenarioParameters scenarioParameters) {
        this.scenarioParameters = scenarioParameters;
        distanceElement = new DistanceElement(numVehicles, size);
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

    public WaitingTimesElement getWaitingTimes() {
        return waitingTimes;
    }

    public DistanceElement getDistanceElement() {
        return distanceElement;
    }
}
