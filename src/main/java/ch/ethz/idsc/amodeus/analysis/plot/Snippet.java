package ch.ethz.idsc.amodeus.analysis.plot;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.img.ColorDataGradient;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;

/* package */ enum StaticHelperColor {

    ;
    /* package */ static final ColorDataGradient FALLBACK = ColorDataGradient.of(Array.zeros(2, 4));
    // ---
    private static final Tensor TRANSPARENT = Tensors.vectorDouble(0, 0, 0, 0).unmodifiable();

    /* package */ static Tensor transparent() {
        return TRANSPARENT.copy();
    }

    static String colorlist(String name) {
        if (name.charAt(0) == '_')
            name = name.substring(1);
        return "/colorlist/" + name.toLowerCase() + ".csv";
    }

    /** @param alpha
     * @return operator that maps a vector of the form rgba to rgb,alpha */
    static TensorUnaryOperator withAlpha(int alpha) {
        Scalar scalar = RealScalar.of(alpha);
        return rgba -> rgba.extract(0, 3).append(scalar);
    }
}
