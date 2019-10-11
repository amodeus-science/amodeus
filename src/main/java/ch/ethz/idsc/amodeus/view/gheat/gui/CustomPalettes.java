/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import java.awt.Color;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorFormat;
import ch.ethz.idsc.tensor.img.Hue;
import ch.ethz.idsc.tensor.img.StrictColorDataIndexed;

/* package */ enum CustomPalettes {
    ;

    public static ColorDataIndexed createOrange() {
        Tensor matrix = Tensors.reserve(256);
        for (int c = 0; c < 255; ++c)
            matrix.append(ColorFormat.toVector(Hue.of(.11111, 1 - c / 256., 1, (1 - c / 256.))));
        matrix.append(ColorFormat.toVector(new Color(0, 0, 0, 0)));
        return StrictColorDataIndexed.of(matrix);
    }

    public static ColorDataIndexed createGreen() {
        Tensor matrix = Tensors.reserve(256);
        for (int c = 0; c < 255; ++c)
            matrix.append(ColorFormat.toVector(Hue.of(.33333, 1 - c / 256., 1, (1 - c / 256.))));
        matrix.append(ColorFormat.toVector(new Color(0, 0, 0, 0)));
        return StrictColorDataIndexed.of(matrix);
    }

    public static ColorDataIndexed createBlack() {
        Tensor matrix = Tensors.reserve(256);
        for (int c = 0; c < 256; ++c)
            matrix.append(ColorFormat.toVector(new Color(0, 0, 0, 255 - c)));
        return StrictColorDataIndexed.of(matrix);
    }

    public static ColorDataIndexed createOrangeContour() {
        Tensor matrix = Tensors.reserve(256);
        for (int c = 0; c < 256; ++c)
            matrix.append(ColorFormat.toVector(c < 192 ? new Color(255, 159, 86, 128) : new Color(0, 0, 0, 0)));
        return StrictColorDataIndexed.of(matrix);
    }

    public static ColorDataIndexed createGreenContour() {
        Tensor matrix = Tensors.reserve(256);
        for (int c = 0; c < 256; ++c)
            matrix.append(ColorFormat.toVector(c < 192 ? new Color(0, 255, 0, 128) : new Color(0, 0, 0, 0)));
        return StrictColorDataIndexed.of(matrix);
    }

}
