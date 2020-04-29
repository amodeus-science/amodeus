/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.UnitSaveUtils;
import ch.ethz.idsc.amodeus.analysis.plot.AmodeusChartUtils;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.red.Max;

public enum WaitingCustomerExport implements AnalysisExport {
    INSTANCE;

    private final String identifier = "waitingCustPerTime";
    public static final String FILENAME = "numberCustomersPlot";
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis tta = analysisSummary.getTravelTimeAnalysis();

        /** save graphics */
        double maxWaiting = tta.waitingCustomers.flatten(-1) // integer value, double for compatibility
                .reduce(Max::of).get().Get().number().doubleValue();

        Tensor values = tta.waitingCustomers;
        values = AnalysisMeanFilter.of(values);
        VisualSet visualSet = new VisualSet(colorDataIndexed);
        visualSet.add(tta.time, values);

        visualSet.setPlotLabel("Waiting Customers per Day Time");
        visualSet.setAxesLabelX("Time");
        visualSet.setAxesLabelY("Waiting Customers [#]");

        JFreeChart chart = ch.ethz.idsc.tensor.fig.TimedChart.of(visualSet);
        chart.getXYPlot().getRangeAxis().setRange(0., maxWaiting + 1);

        try {
            File fileChart = new File(relativeDirectory, FILENAME + ".png");
            AmodeusChartUtils.saveAsPNG(chart, fileChart.toString(), WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILENAME + ".png");
        } catch (Exception e) {
            System.err.println("Plotting " + FILENAME + " failed");
            e.printStackTrace();
        }

        /** save information for processing in other tools */
        try {
            /** request information */
            Tensor table = Transpose.of(Tensors.of(tta.time, tta.waitingCustomers));
            UnitSaveUtils.saveFile(table, identifier, relativeDirectory);
            File dataFolder = new File(relativeDirectory, identifier);
            GlobalAssert.that(dataFolder.isDirectory());
            SaveFormats.CSV.save(Tensors.fromString("time, # waiting customers"), dataFolder, "description");
        } catch (Exception e) {
            System.err.println("Error saving the waiting customers per time step.");
            e.printStackTrace(System.out);
        }
    }
}
