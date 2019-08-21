/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.TableBuilder;

/** The {@link DistancesOverDayTable} exports a table which for every time step
 * contains the total distance, distance with customer, pickup ditance and rebalancing
 * distance driven as well as the ratio between productive distance (with customer) and
 * empty distance (pickup and rebalancing) */
/* package */ enum DistancesOverDayTable implements AnalysisExport {
    INSTANCE;

    private static final String identifier = "DistancesOverDay";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < de.time.length(); ++index)
            tableBuilder.appendRow(de.time.Get(index), de.distancesOverDay.get(index));
        try {
            UnitSaveUtils.saveFile(tableBuilder.getTable(), identifier, relativeDirectory);
            File dataFolder = new File(relativeDirectory, identifier);
            SaveFormats.CSV.save(Tensors.fromString("time step, total distance, distance with customer, "//
                    + "pickup distance, rebalancing distance, distance ratio"), //
                    dataFolder, "description");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
