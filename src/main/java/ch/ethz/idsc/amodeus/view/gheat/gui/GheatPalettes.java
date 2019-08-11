/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.alg.Reverse;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.StrictColorDataIndexed;
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
        tensor.set(Reverse.of(Range.of(0, 256)), Tensor.ALL, 3);
        colorDataIndexed = StrictColorDataIndexed.of(tensor);
    }

}
