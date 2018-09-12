/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.UnitSaveUtils;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensors;

public enum TravelTimeExport implements AnalysisExport {
    INSTANCE;

    private final String identifier = "requestTimeStamps";

    @Override
    public void summaryTarget(AnalysisSummary anlysSmry, File relDir, ColorScheme cScheme) {
        TravelTimeAnalysis travelTime = anlysSmry.getTravelTimeAnalysis();

        /** save information for processing in other tools */
        try {
            /** request information */
            UnitSaveUtils.saveFile(travelTime.requstStmps.toTable(), identifier, relDir);
            File dataFolder = new File(relDir, identifier);
            GlobalAssert.that(dataFolder.isDirectory());
            SaveFormats.CSV.save(//
                    Tensors.fromString("{\"request index\",\"submission time\",\"assignment time\",\"pickup time\",\"dropoff time\"}"), //
                    dataFolder, "description");

            /** figures */

        } catch (Exception e) {
            System.out.println("Error saving the requestTimeStamps");
            e.printStackTrace(System.out);
        }
    }
}
