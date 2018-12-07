/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.plot.DiagramSettings;
import ch.ethz.idsc.amodeus.analysis.plot.HistogramPlot;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Round;

/* package */ enum HistogramReportFigure {
    ;

    private static final int binNmbr = 30;

    /** saves a histogram for the report with the values in @param vals {@link Tensor}
     * of format {v1,v2,v3,..,vN} with the maximum value @param maxVal in
     * the @param relativeDirectory and the name @param fileName
     * 
     * @param colorScheme
     * @param title
     * @param yLabel */
    public static void of(Tensor vals, Scalar maxVal, //
            ColorDataIndexed colorDataIndexed, File relativeDirectory, String title, String yLabel, String fileName) {
        /** normally take integer valued bins */
        Scalar binNmbrScaling = RealScalar.of(1.0 / binNmbr);
        Scalar binSize = Round.of(maxVal.multiply(binNmbrScaling));
        /** for very low values, resolve in decimal steps */
        if (((Quantity) binSize).value().equals(RealScalar.ZERO))
            binSize = maxVal.multiply(binNmbrScaling);
        Tensor binCounter = BinCounts.of(vals, binSize);
        binCounter = binCounter.divide(RealScalar.of(vals.length()));
        try {
            HistogramPlot.of( //
                    binCounter.multiply(RealScalar.of(100)), relativeDirectory, //
                    fileName, title, binSize.number().doubleValue(), "% of requests", //
                    yLabel, DiagramSettings.WIDTH, DiagramSettings.HEIGHT, colorDataIndexed);
        } catch (Exception e) {
            System.err.println("Plotting " + fileName + " failed");
            e.printStackTrace();
        }
    }
}
