/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert; // also exists in subare
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Transpose;

public class VisualRow {
    private Tensor domain = Tensors.empty();
    private Tensor values = Tensors.empty();
    private ComparableLabel comparableLabel = null;
    private Color color = Color.BLUE;
    private Stroke stroke = new BasicStroke(2f);

    public VisualRow() {
    }

    public VisualRow(Tensor domain, Tensor values) {
        add(domain, values);
    }

    public VisualRow(Tensor points) {
        add(points);
    }

    public VisualRow add(Tensor domain, Tensor values) {
        GlobalAssert.that(Dimensions.of(domain).equals(Dimensions.of(values)));
        if (Dimensions.of(domain).isEmpty()) { // if only a single point is provided
            domain = Tensors.of(domain);
            values = Tensors.of(values);
        }
        GlobalAssert.that(Dimensions.of(domain).size() == 1 && Dimensions.of(domain).equals(Dimensions.of(values)));
        this.domain = Join.of(this.domain, domain);
        this.values = Join.of(this.values, values);
        return this;
    }

    public VisualRow add(Tensor points) {
        if (Dimensions.of(points).size() == 1) // if only a single point is provided
            points = Tensors.of(points);
        GlobalAssert.that(Dimensions.of(points).size() == 2 && Dimensions.of(points).get(1) == 2);
        domain = Join.of(domain, Transpose.of(points).get(0));
        values = Join.of(values, Transpose.of(points).get(1));
        return this;
    }

    public VisualRow add(Scalar x, Scalar y) {
        return add(Tensors.of(x, y));
    }

    public Tensor getDomain() {
        return domain;
    }

    public Tensor getValues() {
        return values;
    }

    public Color getColor() {
        return color;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public ComparableLabel getLabel() {
        return comparableLabel;
    }

    public String getLabelString() {
        return getLabel().toString();
    }

    public boolean hasLabel() {
        return comparableLabel != null;
    }

    public void setLabel(ComparableLabel comparableLabel) {
        this.comparableLabel = comparableLabel;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }
}
