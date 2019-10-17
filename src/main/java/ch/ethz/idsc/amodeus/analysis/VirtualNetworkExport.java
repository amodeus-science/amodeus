/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.MultiFileReader;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

/* package */ class VirtualNetworkExport implements AnalysisExport {
    private final ScenarioOptions scenarioOptions;

    public VirtualNetworkExport(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override // from AnalysisExport
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        final File virtualNetworkFolder = new File(scenarioOptions.getVirtualNetworkDirectoryName());
        System.out.println("virtualNetworkFolder:  " + virtualNetworkFolder.getAbsolutePath());
        try {//
            File copyToDir = new File(relativeDirectory, virtualNetworkFolder.getName());
            copyToDir.delete();
            copyToDir.mkdirs();
            for (File file : new MultiFileReader(virtualNetworkFolder).getFolderFiles()) {
                Files.copy(file, new File(copyToDir, file.getName()));
            }
        } catch (IOException exception) {
            System.err.println("The virtual network file was not copied to the data directory...");
            System.err.println("Some I/O error, check class ch.ethz.idsc.amodeus.analysis.VirtualNetworkExport");
            exception.printStackTrace();
        }
    }
}
