/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.io.Export;

public class ScenarioParametersExport implements AnalysisExport {
    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory) {
        ScenarioParameters scenarioParameters = analysisSummary.getScenarioParameters();
        try {
            Export.object(new File(relativeDirectory, "scenarioParameters.obj"), scenarioParameters);
        } catch (Exception e1) {
            System.err.println("Scenario Parameters could not be found");
            e1.printStackTrace();
        }
    }

}
