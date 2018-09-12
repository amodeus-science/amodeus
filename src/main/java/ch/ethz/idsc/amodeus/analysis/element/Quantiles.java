package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ enum Quantiles {
    ;
    public static final double LOW = 0.1;
    public static final double MID = 0.5;
    public static final double UPR = 0.95;
    public static final Tensor SET = Tensors.vector(LOW, MID, UPR);
    public static final String[] LBL = //
            new String[] { (LOW * 100) + "% quantile", (MID * 100) + "% quantile", (UPR * 100) + "% quantile", "Mean" };
}
