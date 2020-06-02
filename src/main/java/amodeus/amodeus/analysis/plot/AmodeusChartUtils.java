package amodeus.amodeus.analysis.plot;

import java.io.File;

import org.jfree.chart.JFreeChart;
import org.matsim.contrib.util.chart.ChartSaveUtils;

public class AmodeusChartUtils {
    static public void saveAsPNG(JFreeChart chart, String filename, int width, int height) {
        ChartSaveUtils.saveAsPNG(chart, filename.replace(".png", ""), width, height);
    }

    static public void saveChartAsPNG(File fileChart, JFreeChart chart, int width, int height) {
        saveAsPNG(chart, fileChart.toString(), width, height);
    }
}
