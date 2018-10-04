/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.Serializable;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.analysis.element.RequestRobotaxiInformationElement;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;

public class AnalysisSummary implements Serializable {

    private final ScenarioParameters scenarioParameters = new ScenarioParameters();
    private final RequestRobotaxiInformationElement simulationInformationElement = new RequestRobotaxiInformationElement();
    private final StatusDistributionElement statusDistribution = new StatusDistributionElement();
    private final DistanceElement distanceElement;
    private final TravelTimeAnalysis travelTimeAnalysis = new TravelTimeAnalysis();
    private final NumberPassengersAnalysis numberPassengersAnalysis = new NumberPassengersAnalysis();

    // TODO Claudio, is public required here? We normally use it as a Part of the Analysis()
    // Thus an initialization of AnalysisSummary() is superficial..
    public AnalysisSummary(int numVehicles, int size, MatsimStaticDatabase db) {
        distanceElement = new DistanceElement(numVehicles, size, db);
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

    public NumberPassengersAnalysis getNumberPassengersAnalysis() {
        return numberPassengersAnalysis;
    }
}
