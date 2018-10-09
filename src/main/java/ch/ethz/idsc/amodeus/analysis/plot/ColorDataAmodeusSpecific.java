/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.StrictColorDataIndexed;
import ch.ethz.idsc.tensor.io.ResourceData;

public enum ColorDataAmodeusSpecific {
    COLORFUL, //
    NONE, //
    STANDARD, //
    POP, //
    MILD, //
    ;
    private final Tensor tensor = ResourceData.of(StaticHelperColor.colorlist(name()));
    private final ColorDataIndexed colorDataIndexed = StrictColorDataIndexed.create(tensor);

    /** @return */
    public ColorDataIndexed strict() {
        return colorDataIndexed;
    }

    /** @return number of unique colors before cycling */
    public int size() {
        return tensor.length();
    }

}
