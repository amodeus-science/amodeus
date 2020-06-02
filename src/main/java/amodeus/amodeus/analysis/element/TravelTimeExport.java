/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import java.io.File;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.UnitSaveUtils;
import amodeus.amodeus.util.io.SaveFormats;
import amodeus.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

/** {@link TravelTimeExport} saves a table called in which each reqest is listed with its
 * index, its submission time, (yougest) assignment time, pickup time and dropoff
 * time. The data can be used to create custom statistics for a simulation. */
public enum TravelTimeExport implements AnalysisExport {
    INSTANCE;

    private final String identifier = "RequestTravelTimes";

    @Override
    public void summaryTarget(AnalysisSummary anlysSmry, File relDir, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis travelTime = anlysSmry.getTravelTimeAnalysis();
        /** save information for processing in other tools */
        try {
            /** request information */
            UnitSaveUtils.saveFile(travelTime.requstStmps.getTable(), identifier, relDir);
            File dataFolder = new File(relDir, identifier);
            GlobalAssert.that(dataFolder.isDirectory());
            SaveFormats.CSV.save(Tensors.fromString("request index, submission time, "//
                    + "assignment time, pickup time, dropoff time"), //
                    dataFolder, "description");
        } catch (Exception e) {
            System.err.println("Error saving the travel time information for every request.");
            e.printStackTrace(System.out);
        }
    }
}
