/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.StrictColorDataIndexed;
import ch.ethz.idsc.tensor.io.Primitives;
import ch.ethz.idsc.tensor.io.ResourceData;

/* package */ enum GheatPalettes {
    FIRE, //
    PBJ, //
    PGAITCH, //
    OMG, //
    ;

    public final ColorDataIndexed colorDataIndexed;

    private GheatPalettes() {
        Tensor tensor = ResourceData.of("/colorscheme/" + name().toLowerCase() + ".csv");
        int[][] sum = Primitives.toIntArray2D(tensor);
        Tensor matrix = Tensors.reserve(256);
        for (int c = 0; c < 256; ++c)
            matrix.append(Tensors.vector(sum[c][0], sum[c][1], sum[c][2], 255 - c));
        colorDataIndexed = StrictColorDataIndexed.create(matrix);
    }

}
