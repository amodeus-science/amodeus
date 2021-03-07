/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.tensor.fig;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import amodeus.amodeus.util.math.Scalar2Number;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/* package */ enum StaticHelper {
    ;
    public static CategoryDataset defaultCategoryDataset(VisualSet visualSet) {
        return defaultCategoryDataset(visualSet, Scalar::toString);
    }

    public static CategoryDataset defaultCategoryDataset(VisualSet visualSet, Function<Scalar, String> naming) {
        DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
        for (VisualRow visualRow : visualSet.visualRows())
            for (Tensor point : visualRow.points())
                defaultCategoryDataset.addValue( //
                        Scalar2Number.of(point.Get(1)).doubleValue(), //
                        visualRow.getLabel(), //
                        naming.apply(point.Get(0)));
        return defaultCategoryDataset;
    }

    /** Quote from the JFreeChart javadoc: "[...] The {@link TableXYDataset}
     * interface requires all series to share the same set of x-values. When
     * adding a new item <code>(x, y)</code> to one series, all other series
     * automatically get a new item <code>(x, null)</code> unless a non-null item
     * has already been specified."
     * 
     * @param visualSet
     * @return */
    public static TableXYDataset categoryTableXYDataset(VisualSet visualSet) {
        CategoryTableXYDataset categoryTableXYDataset = new CategoryTableXYDataset();
        for (VisualRow visualRow : visualSet.visualRows()) {
            String label = visualRow.getLabelString().isEmpty() //
                    ? String.valueOf(categoryTableXYDataset.getSeriesCount())
                    : visualRow.getLabelString();
            for (Tensor point : visualRow.points())
                categoryTableXYDataset.add( //
                        Scalar2Number.of(point.Get(0)).doubleValue(), //
                        Scalar2Number.of(point.Get(1)).doubleValue(), //
                        label); // requires string, might lead to overwriting
        }
        return categoryTableXYDataset;
    }

    public static TimeTableXYDataset timeTableXYDataset(VisualSet visualSet) {
        TimeTableXYDataset timeTableXYDataset = new TimeTableXYDataset();
        for (VisualRow visualRow : visualSet.visualRows())
            for (Tensor point : visualRow.points())
                timeTableXYDataset.add( //
                        toTime(point.Get(0)), //
                        Scalar2Number.of(point.Get(1)).doubleValue(), //
                        visualRow.getLabel());
        return timeTableXYDataset;
    }

    /** Quote from the JFreeChart javadoc: "[XYSeries] represents a sequence of zero
     * or more data items in the form (x, y). By default, items in the series will be
     * sorted into ascending order by x-value, and duplicate x-values are permitted."
     *
     * @param visualSet
     * @return */
    public static XYSeriesCollection xySeriesCollection(VisualSet visualSet) {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        for (VisualRow visualRow : visualSet.visualRows()) {
            String labelString = visualRow.getLabelString();
            XYSeries xySeries = new XYSeries(labelString.isEmpty() ? xySeriesCollection.getSeriesCount() : labelString);
            for (Tensor point : visualRow.points())
                xySeries.add(Scalar2Number.of(point.Get(0)), Scalar2Number.of(point.Get(1)));
            xySeriesCollection.addSeries(xySeries);
        }
        return xySeriesCollection;
    }

    public static TimeSeriesCollection timeSeriesCollection(VisualSet visualSet) {
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        for (VisualRow visualRow : visualSet.visualRows()) {
            String labelString = visualRow.getLabelString();
            TimeSeries timeSeries = new TimeSeries(labelString.isEmpty() ? timeSeriesCollection.getSeriesCount() : labelString);
            for (Tensor point : visualRow.points())
                try {
                    timeSeries.add(toTime(point.Get(0)), Scalar2Number.of(point.Get(1)));
                } catch (Exception e) {
                    // sensitive to overwriting as smallest unit is second (integer)
                    e.printStackTrace();
                }
            timeSeriesCollection.addSeries(timeSeries);
        }
        return timeSeriesCollection;
    }

    // from StaticHelper
    /* package for testing */ static Second toTime(Scalar time) {
        long timeL = Scalar2Number.of(time).longValue();
        int days = (int) TimeUnit.SECONDS.toDays(timeL);
        int hours = (int) (TimeUnit.SECONDS.toHours(timeL) - 24.0 * days);
        int minutes = (int) (TimeUnit.SECONDS.toMinutes(timeL) - 60.0 * hours - 1440.0 * days);
        int seconds = (int) (TimeUnit.SECONDS.toSeconds(timeL) - 60.0 * minutes - 3600.0 * hours - 86400.0 * days);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1; // Month are 0 based, thus it is necessary to add 1
        return new Second(seconds, minutes, hours, days + 1, month, year); // day, month and year can not be zero
    }
}
