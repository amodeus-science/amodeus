/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Objects;

import ch.ethz.idsc.tensor.Tensor;

public class VisualRow {
    private final Tensor points;
    private final ComparableLabel comparableLabel;
    private Color color = Color.BLUE;
    private Stroke stroke = new BasicStroke(2f);

    /** Mathematica::ListPlot[points]
     * 
     * @param points of the form {{x1, y1}, {x2, y2}, ..., {xn, yn}}
     * @return */
    VisualRow(Tensor points, int index) {
        this.points = points;
        this.comparableLabel = new ComparableLabel(index);
    }

    /** @return points of the form {{x1, y1}, {x2, y2}, ..., {xn, yn}} */
    public Tensor points() {
        return points.unmodifiable();
    }

    public Tensor domain() {
        return points.get(Tensor.ALL, 0).unmodifiable();
    }

    public Tensor values() {
        return points.get(Tensor.ALL, 1).unmodifiable();
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

    public void setLabel(String string) {
        comparableLabel.setString(string);
    }

    public String getLabelString() {
        return getLabel().toString();
    }

    /* package */ ComparableLabel getLabel() {
        return comparableLabel;
    }
}
