/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.test;

import java.io.File;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.element.AnalysisExport;
import amodeus.amodeus.analysis.element.DistanceElement;
import amodeus.amodeus.analysis.element.NumberPassengersAnalysis;
import amodeus.amodeus.analysis.element.RequestRobotaxiInformationElement;
import amodeus.amodeus.analysis.element.StatusDistributionElement;
import amodeus.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public class AnalysisTestExport implements AnalysisExport {

    private RequestRobotaxiInformationElement simulationInformationElement;
    private StatusDistributionElement statusDistribution;
    private DistanceElement distanceElement;
    private TravelTimeAnalysis travelTimeAnalysis;
    private NumberPassengersAnalysis numberPassengersAnalysis;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        simulationInformationElement = analysisSummary.getSimulationInformationElement();
        statusDistribution = analysisSummary.getStatusDistribution();
        distanceElement = analysisSummary.getDistanceElement();
        travelTimeAnalysis = analysisSummary.getTravelTimeAnalysis();
        numberPassengersAnalysis = analysisSummary.getNumberPassengersAnalysis();
    }

    public RequestRobotaxiInformationElement getSimulationInformationElement() {
        return simulationInformationElement;
    }

    public StatusDistributionElement getStatusDistribution() {
        return statusDistribution;
    }

    public DistanceElement getDistancElement() {
        return distanceElement;
    }

    public TravelTimeAnalysis getTravelTimeAnalysis() {
        return travelTimeAnalysis;
    }

    public NumberPassengersAnalysis getNumberPassengersAnalysis() {
        return numberPassengersAnalysis;
    }
}
