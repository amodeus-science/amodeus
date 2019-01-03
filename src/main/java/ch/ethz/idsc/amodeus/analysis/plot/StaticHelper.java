/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.MeanFilter;

/* package */ enum StaticHelper {
    ;

    public static Second toTime(double time) {
        long timeL = (long) time;
        int hours = (int) TimeUnit.SECONDS.toHours(timeL);
        int minutes = (int) (TimeUnit.SECONDS.toMinutes(timeL) - 60.0 * hours);
        int seconds = (int) (TimeUnit.SECONDS.toSeconds(timeL) - minutes * 60.0 - hours * 3600.0);
        Second second = new Second(seconds, minutes, hours, 1, 1, 1900); // month and year can not be zero
        return second;
    }

    public static String fileTitle(String diagramTitle) {
        return diagramTitle.replaceAll("\\s+", "");
    }

    public static File savePlot(File directory, String fileTitle, JFreeChart chart, int width, int height) throws Exception {
        File fileChart = new File(directory, fileTitle + ".png");
        ChartUtilities.saveChartAsPNG(fileChart, chart, width, height);
        GlobalAssert.that(fileChart.isFile());
        System.out.println("Exported " + fileTitle + ".png");
        return fileChart;
    }

    public static Tensor filtered(Tensor values, int filterSize) {
        Tensor valuesFiltered = Tensors.empty();
        for (int i = 0; i < Transpose.of(values).length(); ++i) {
            valuesFiltered.append(MeanFilter.of(Transpose.of(values).get(i), filterSize));
        }
        return Transpose.of(valuesFiltered);
    }

}
