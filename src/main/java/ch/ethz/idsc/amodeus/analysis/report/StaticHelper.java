package ch.ethz.idsc.amodeus.analysis.report;


import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.MeanFilter;

public enum StaticHelper {
    ;

    /** for small scenarios, a filter is necessary to obain smooth waiting times plots */
    public static final int FILTERSIZE = 50;
    public static final boolean FILTER_ON = true;
    
    public static Tensor filtered(Tensor values, int filterSize) {
        Tensor valuesFiltered = Tensors.empty();
        for (int i = 0; i < Transpose.of(values).length(); ++i) {
            valuesFiltered.append(MeanFilter.of(Transpose.of(values).get(i), filterSize));
        }
        return Transpose.of(valuesFiltered);
    }


}
