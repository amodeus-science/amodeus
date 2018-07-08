/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import java.awt.Color;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.Primitives;
import ch.ethz.idsc.tensor.io.ResourceData;

/* package */ enum GheatPalettes {
    FIRE, //
    PBJ, //
    PGAITCH, //
    OMG, //
    ;

    public final ColorScheme colorScheme;

    private GheatPalettes() {
        colorScheme = new ColorScheme();
        Tensor tensor = ResourceData.of("/colorscheme/" + name().toLowerCase() + ".csv");
        int[][] sum = Primitives.toIntArray2D(tensor);
        for (int c = 0; c < 256; ++c)
            colorScheme.set(c, new Color(sum[c][0], sum[c][1], sum[c][2], 255 - c));
    }

}
