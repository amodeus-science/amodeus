/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.plot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
// import org.matsim.contrib.util.chart.ChartSaveUtils;

public class AmodeusChartUtils {
    static public void saveAsPNG(JFreeChart chart, String filename, int width, int height) {
        // ChartSaveUtils.saveAsPNG(chart, filename.replace(".png", ""), width, height); // FIXME stream is not properly closed, causing problems on Windows
        try(FileOutputStream fout = new FileOutputStream(filename)) {
            ChartUtils.writeChartAsPNG(fout, chart, width, height);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    static public void saveChartAsPNG(File fileChart, JFreeChart chart, int width, int height) {
        saveAsPNG(chart, fileChart.toString(), width, height);
    }
}
