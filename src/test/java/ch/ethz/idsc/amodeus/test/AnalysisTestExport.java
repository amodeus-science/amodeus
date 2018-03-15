/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.element.RequestRobotaxiInformationElement;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.element.WaitingTimesElement;

public class AnalysisTestExport implements AnalysisExport {

    private RequestRobotaxiInformationElement simulationInformationElement;
    private StatusDistributionElement statusDistribution;
    private DistanceElement distanceElement;
    private WaitingTimesElement waitingTimes;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory) {
        simulationInformationElement = analysisSummary.getSimulationInformationElement();
        statusDistribution = analysisSummary.getStatusDistribution();
        distanceElement = analysisSummary.getDistanceElement();
        waitingTimes = analysisSummary.getWaitingTimes();
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

    public WaitingTimesElement getWaitingTimes() {
        return waitingTimes;
    }

}
