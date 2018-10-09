/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.Export;

public enum ScenarioParametersExport implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "scenarioParameters.obj";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        ScenarioParameters scenarioParameters = analysisSummary.getScenarioParameters();

        try {
            Export.object(new File(relativeDirectory, FILENAME), scenarioParameters);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException();
        }

    }

}
