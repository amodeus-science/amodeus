/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.service;

import java.io.File;
import java.util.Collections;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public class RequestHistoriesExportFromEvents implements AnalysisExport {

    private static final String REQUEST_HISTORY_CSV = "requestHistory.csv";
    // ---
    private final Network network;
    private final String eventFile;

    public RequestHistoriesExportFromEvents(Network network, Config config) {
        this.network = network;
        eventFile = config.controler().getOutputDirectory() + "/output_events.xml.gz";
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        try {
            ConvertAVServicesFromEvents.write(network, relativeDirectory.getAbsolutePath() + "/" + REQUEST_HISTORY_CSV, eventFile,
                    Collections.singleton(AmodeusModeConfig.DEFAULT_MODE));
        } catch (Exception e) {
            System.err.println("It was not possible to create the " + REQUEST_HISTORY_CSV);
        }
    }

}
