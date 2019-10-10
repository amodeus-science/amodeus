/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.jfree.data.time.Second;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.MeanFilter;

/* package */ enum StaticHelper {
    ;

    public static Second toTime(double time) {
        long timeL = (long) time;
        int day = 1;
        int hours = (int) TimeUnit.SECONDS.toHours(timeL);
        int minutes = (int) (TimeUnit.SECONDS.toMinutes(timeL) - 60.0 * hours);
        int seconds = (int) (TimeUnit.SECONDS.toSeconds(timeL) - minutes * 60.0 - hours * 3600.0);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1; // Month are 0 based, thus it is nesscessary to add 1
        Second second = new Second(seconds, minutes, hours, day, month, year); // month and year can not be zero
        return second;
    }

    public static String fileTitle(String diagramTitle) {
        return diagramTitle.replaceAll("\\s+", "");
    }

    public static Tensor filtered(Tensor values, int filterSize) {
        return Transpose.of(Tensor.of(Transpose.of(values).stream() //
                .map(row -> MeanFilter.of(row, filterSize))));
    }

}
