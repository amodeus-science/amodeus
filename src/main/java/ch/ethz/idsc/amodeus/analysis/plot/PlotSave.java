/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum PlotSave {
    ;
    public static File now(File directory, String fileTitle, JFreeChart chart, int width, int height) //
            throws Exception {
        File fileChart = new File(directory, fileTitle + ".png");
        ChartUtilities.saveChartAsPNG(fileChart, chart, width, height);
        GlobalAssert.that(fileChart.isFile());
        System.out.println("Exported " + fileTitle + ".png");
        return fileChart;
    }

}
