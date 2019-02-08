/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Objects;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;

public class VisualRow {
    private final Tensor points;
    private Color color = Color.BLUE;
    private Stroke stroke = new BasicStroke(2f);
    private ComparableLabel comparableLabel = null;

    /** MATLAB::plot(x, y)
     * 
     * @param domain {x1, x2, ..., xn}
     * @param values {y1, y2, ..., yn}
     * @return */
    public VisualRow(Tensor domain, Tensor values) {
        points = Transpose.of(Tensors.of(domain, values));
    }

    /** Mathematica::ListPlot[points]
     * 
     * @param points of the form {{x1, y1}, {x2, y2}, ..., {xn, yn}}
     * @return */
    public VisualRow(Tensor points) {
        this.points = points;
    }

    public Tensor points() {
        return points;
    }

    public void setColor(Color color) {
        this.color = Objects.requireNonNull(color);
    }

    public Color getColor() {
        return color;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = Objects.requireNonNull(stroke);
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setLabel(ComparableLabel comparableLabel) {
        this.comparableLabel = Objects.requireNonNull(comparableLabel);
    }

    public void setLabel(String string) {
        comparableLabel.setString(string);
    }

    public ComparableLabel getLabel() {
        return comparableLabel;
    }

    public String getLabelString() {
        return getLabel().toString();
    }

    public boolean hasLabel() {
        return Objects.nonNull(comparableLabel);
    }

}
