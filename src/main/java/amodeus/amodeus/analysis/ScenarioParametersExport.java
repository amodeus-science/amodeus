/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis;

import java.io.File;

import amodeus.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.Export;

/* package */ enum ScenarioParametersExport implements AnalysisExport {
    INSTANCE;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        ScenarioParameters scenarioParameters = analysisSummary.getScenarioParameters();

        try {
            Export.object(new File(relativeDirectory, AnalysisConstants.ParametersExportFilename), scenarioParameters);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException();
        }

    }

}
