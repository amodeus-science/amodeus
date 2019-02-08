/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Objects;

import ch.ethz.idsc.tensor.Tensor;

public class VisualRow {
    private final Tensor points;
    private Color color = Color.BLUE;
    private Stroke stroke = new BasicStroke(2f);
    private ComparableLabel comparableLabel;

    /** Mathematica::ListPlot[points]
     * 
     * @param points of the form {{x1, y1}, {x2, y2}, ..., {xn, yn}}
     * @return */
    VisualRow(Tensor points) {
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
