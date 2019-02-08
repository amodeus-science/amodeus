/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.TableXYDataset;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.MatrixQ;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.sca.Clip;

public class VisualSet {
    private final List<VisualRow> visualRows = new ArrayList<>();
    private final ColorDataIndexed colorDataIndexed;
    private String plotLabel = "";
    private String domainAxisLabel = "";
    private String rangeAxisLabel = "";

    public VisualSet(ColorDataIndexed colorDataIndexed) {
        this.colorDataIndexed = Objects.requireNonNull(colorDataIndexed);
    }

    /** uses Mathematica default color scheme */
    public VisualSet() {
        this(ColorDataLists._097.cyclic());
    }

    /** @param points of the form {{x1, y1}, {x2, y2}, ..., {xn, yn}}
     * @return */
    public VisualRow add(Tensor points) {
        final int index = visualRows.size();
        VisualRow visualRow = new VisualRow(MatrixQ.require(points), index);
        visualRow.setColor(colorDataIndexed.getColor(index));
        visualRows.add(visualRow);
        return visualRow;
    }

    /** @param domain {x1, x2, ..., xn}
     * @param values {y1, y2, ..., yn}
     * @return */
    public VisualRow add(Tensor domain, Tensor values) {
        return add(Transpose.of(Tensors.of(domain, values)));
    }

    public List<VisualRow> visualRows() {
        return Collections.unmodifiableList(visualRows);
    }

    public VisualRow get(int index) {
        return visualRows.get(index);
    }

    public String getPlotLabel() {
        return plotLabel;
    }

    public String getDomainAxisLabel() {
        return domainAxisLabel;
    }

    public String getRangeAxisLabel() {
        return rangeAxisLabel;
    }

    public boolean hasLegend() {
        return visualRows.stream().anyMatch(visualRow -> StringUtils.isNotEmpty(visualRow.getLabelString()));
    }

    public void setPlotLabel(String string) {
        plotLabel = string;
    }

    public void setDomainAxisLabel(String string) {
        domainAxisLabel = string;
    }

    public void setRangeAxisLabel(String string) {
        rangeAxisLabel = string;
    }

    // TODO is there a way to make better use of similarity?

    public CategoryDataset categorical() {
        return categorical(Scalar::toString);
    }

    public CategoryDataset categorical(Function<Scalar, String> naming) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (VisualRow visualRow : visualRows)
            for (Tensor point : visualRow.points())
                dataset.addValue(point.Get(1).number().doubleValue(), //
                        visualRow.getLabel(), //
                        naming.apply(point.Get(0)));
        return dataset;
    }

    public TableXYDataset timed() {
        final TimeTableXYDataset dataset = new TimeTableXYDataset();
        for (VisualRow visualRow : visualRows)
            for (Tensor point : visualRow.points())
                dataset.add(toTime(point.Get(0)), //
                        point.Get(1).number().doubleValue(), //
                        visualRow.getLabel());
        return dataset;
    }

    public TableXYDataset xy() {
        final CategoryTableXYDataset dataset = new CategoryTableXYDataset();
        for (VisualRow visualRow : visualRows) {
            String label = visualRow.getLabelString().isEmpty() //
                    ? String.valueOf(dataset.getSeriesCount())
                    : visualRow.getLabelString();
            for (Tensor point : visualRow.points())
                dataset.add(point.Get(0).number().doubleValue(), //
                        point.Get(1).number().doubleValue(), //
                        label); // requires string, might lead to overwriting
        }
        return dataset;
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
