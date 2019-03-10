/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public class VirtualNetworkExport implements AnalysisExport {
    private final ScenarioOptions scenarioOptions;

    public VirtualNetworkExport(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        final File virtualNetworkFolder = new File(scenarioOptions.getWorkingDirectory(), scenarioOptions.getVirtualNetworkName());

        try {//
            File virtualNetworkFile = new File(virtualNetworkFolder, scenarioOptions.getVirtualNetworkName());
            File copyTo = new File(relativeDirectory, scenarioOptions.getVirtualNetworkName());
            // GlobalAssert.that(virtualNetworkFile.exists());
            // GlobalAssert.that(copyTo.getParentFile().isDirectory());
            System.out.println(virtualNetworkFile);
            System.out.println(copyTo);
            Files.copy(virtualNetworkFile, copyTo);
        } catch (IOException exception) {
            System.err.println("The virtual network file was not copied to the data directory as");
            System.err.println("it was not found. A possible reason is that no virtualnetwork was");
            System.err.println("created in this simulation. ");
        }

        // if (virtualNetworkFolder.isDirectory()) {
        // File virtualNetworkFile = new File(virtualNetworkFolder, scenOptions.getVirtualNetworkName());
        // File copyTo = new File(relativeDirectory, scenOptions.getVirtualNetworkName());
        // GlobalAssert.that(virtualNetworkFile.exists());
        // GlobalAssert.that(copyTo.getParentFile().isDirectory());
        // try {
        // System.out.println(virtualNetworkFile);
        // System.out.println(copyTo);
        // Files.copy(virtualNetworkFile, copyTo);
        // } catch (IOException e) {
        // GlobalAssert.that(false);
        // }
        // } else
        // System.err.println("virtual directory not found");

    }

}
