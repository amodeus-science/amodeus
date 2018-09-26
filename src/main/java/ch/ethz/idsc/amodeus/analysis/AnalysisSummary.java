/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.Serializable;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.analysis.element.RequestRobotaxiInformationElement;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;

public class AnalysisSummary implements Serializable {

    private final ScenarioParameters scenarioParameters = new ScenarioParameters();
    private final RequestRobotaxiInformationElement simulationInformationElement = new RequestRobotaxiInformationElement();
    private final StatusDistributionElement statusDistribution = new StatusDistributionElement();
    private final DistanceElement distanceElement;
    private final TravelTimeAnalysis travelTimeAnalysis;
    private final NumberPassengersAnalysis numberPassengersAnalysis = new NumberPassengersAnalysis();

    public AnalysisSummary(int numVehicles, int size, Network network) {
        distanceElement = new DistanceElement(numVehicles, size);
        /** Standard Least Path Calculator for Standard Travel Times */
        travelTimeAnalysis = new TravelTimeAnalysis(network);
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
