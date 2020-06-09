/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.service;

import java.io.File;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public class RoboTaxiHistoriesExportFromEvents implements AnalysisExport {

    private static final String VEHICLE_ACTIVITIES_HISTORY_CSV = "vehicleActivitiesHistory.csv";
    private static final String VEHICLE_MOVEMENTS_HISTORY_CSV = "vehicleMovementsHistory.csv";

    private final Network network;
    private final String eventFile;

    public RoboTaxiHistoriesExportFromEvents(Network network, Config config) {
        this.network = network;
        eventFile = config.controler().getOutputDirectory() + "/output_events.xml.gz";
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        try {
            ConvertAVTracesFromEvents.write(network, relativeDirectory.getAbsolutePath() + "/" + VEHICLE_ACTIVITIES_HISTORY_CSV,
                    relativeDirectory.getAbsolutePath() + "/" + VEHICLE_MOVEMENTS_HISTORY_CSV, eventFile);
        } catch (Exception e) {
            System.err.println("It was not possible to create the " + VEHICLE_ACTIVITIES_HISTORY_CSV + " / " + VEHICLE_MOVEMENTS_HISTORY_CSV);
        }
    }

}
