package ch.ethz.idsc.amodeus.analysis.service;

import java.io.File;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public class RoboTaxiHistoriesExportFromEvents implements AnalysisExport {

    // TODO Sebastian/Lukas: take from controler or config if possible
    private static final String FILENAME_MANUAL = "/output_events.xml.gz";

    private static final String SERVICEFILENAME = "vehicleHistory.csv";
    private final Network network;
    private final String eventFile;

    public RoboTaxiHistoriesExportFromEvents(Network network, Config config) {
        this.network = network;
        eventFile = config.controler().getOutputDirectory() + FILENAME_MANUAL;
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        try {
            ConvertAVTracesFromEvents.write(network, relativeDirectory.getAbsolutePath() + "/" + SERVICEFILENAME, eventFile);
        } catch (Exception e) {
            System.err.println("It was not possible to create the " + SERVICEFILENAME);
        }
    }

}
