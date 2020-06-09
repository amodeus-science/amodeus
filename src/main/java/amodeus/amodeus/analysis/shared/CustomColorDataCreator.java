/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.shared;

import java.awt.Color;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.StrictColorDataIndexed;

/* package */ class CustomColorDataCreator {
    private Tensor tensor = Tensors.empty();

    public void append(Color color) {
        tensor.append(Tensors.vector(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
    }

    public ColorDataIndexed getColorDataIndexed() {
        return StrictColorDataIndexed.of(tensor);
    }
}
