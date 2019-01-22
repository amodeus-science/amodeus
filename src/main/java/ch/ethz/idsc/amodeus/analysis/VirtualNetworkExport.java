/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum VirtualNetworkExport implements AnalysisExport {
    INSTANCE;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        File workingDirectory = null;
        try {
            workingDirectory = MultiFileTools.getWorkingDirectory();
        } catch (IOException e) {
            GlobalAssert.that(false);
        }
        ScenarioOptions scenOptions = null;
        try {
            scenOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        } catch (IOException e) {
            GlobalAssert.that(false);
        }
        final File virtualNetworkFolder = new File(workingDirectory, scenOptions.getVirtualNetworkName());
        if (virtualNetworkFolder.isDirectory()) {
            File virtualNetworkFile = new File(virtualNetworkFolder, scenOptions.getVirtualNetworkName());
            File copyTo = new File(relativeDirectory, scenOptions.getVirtualNetworkName());
            GlobalAssert.that(virtualNetworkFile.exists());
            GlobalAssert.that(copyTo.getParentFile().isDirectory());
            try {
                System.out.println(virtualNetworkFile);
                System.out.println(copyTo);
                Files.copy(virtualNetworkFile, copyTo);
            } catch (IOException e) {
                GlobalAssert.that(false);
            }
        } else
            System.err.println("virtual directory not found");
    }

}
