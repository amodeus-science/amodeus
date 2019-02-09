/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.TableXYDataset;

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
                        point.Get(1).number().doubleValue(), //
                        visualRow.getLabel(), //
                        naming.apply(point.Get(0)));
        return defaultCategoryDataset;
    }

    public static TableXYDataset timeTableXYDataset(VisualSet visualSet) {
        TimeTableXYDataset timeTableXYDataset = new TimeTableXYDataset();
        for (VisualRow visualRow : visualSet.visualRows())
            for (Tensor point : visualRow.points())
                timeTableXYDataset.add( //
                        toTime(point.Get(0)), //
                        point.Get(1).number().doubleValue(), //
                        visualRow.getLabel());
        return timeTableXYDataset;
    }

    public static TableXYDataset categoryTableXYDataset(VisualSet visualSet) {
        CategoryTableXYDataset categoryTableXYDataset = new CategoryTableXYDataset();
        for (VisualRow visualRow : visualSet.visualRows()) {
            String label = visualRow.getLabelString().isEmpty() //
                    ? String.valueOf(categoryTableXYDataset.getSeriesCount())
                    : visualRow.getLabelString();
            for (Tensor point : visualRow.points())
                categoryTableXYDataset.add( //
                        point.Get(0).number().doubleValue(), //
                        point.Get(1).number().doubleValue(), //
                        label); // requires string, might lead to overwriting
        }
        return categoryTableXYDataset;
    }

    // from StaticHelper
    private static Second toTime(Scalar time) {
        long timeL = time.number().longValue();
        int day = 1;
        int hours = (int) TimeUnit.SECONDS.toHours(timeL);
        int minutes = (int) (TimeUnit.SECONDS.toMinutes(timeL) - 60.0 * hours);
        int seconds = (int) (TimeUnit.SECONDS.toSeconds(timeL) - minutes * 60.0 - hours * 3600.0);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1; // Month are 0 based, thus it is necessary to add 1
        return new Second(seconds, minutes, hours, day, month, year); // month and year can not be zero
    }

}
