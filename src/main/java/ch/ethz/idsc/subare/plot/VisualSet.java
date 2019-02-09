/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.MatrixQ;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;

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

    public VisualRow getVisualRow(int index) {
        return visualRows.get(index);
    }

    public String getPlotLabel() {
        return plotLabel;
    }

    public String getDomainAxisLabel() {
        return domainAxisLabel;
    }

    /** @return name of codomain/target set */
    public String getRangeAxisLabel() {
        return rangeAxisLabel;
    }

    public boolean hasLegend() {
        return visualRows.stream() //
                .map(VisualRow::getLabelString) //
                .anyMatch(string -> !string.isEmpty());
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

}
