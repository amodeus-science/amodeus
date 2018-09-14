/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.ResourceData;
import ch.ethz.idsc.tensor.io.TensorProperties;
import ch.ethz.idsc.tensor.qty.Quantity;

/** values in class are required by AidoHost
 * therefore class was made public */
public class ScoreParameters {
    /** overrides default values defined in class
     * with the values parsed from the properties file */
    public static final ScoreParameters GLOBAL = TensorProperties.wrap(new ScoreParameters()) //
            .set(ResourceData.properties("/aido/ScoreParameters.properties"));

    /** service quality */
    public Tensor alpha12 = Tensors.fromString("{-0.5[s^-1], -0.7[m^-1]}");

    /** efficiency score */
    public Tensor alpha34 = Tensors.fromString("{-0.5[s^-1], -0.5[m^-1]}");

    /** standard fleet size as a fraction of the total number of requests */
    public Scalar gamma = RealScalar.of(0.025);

    /** mean wait time for the fleet reduction */
    public Scalar wmean = Quantity.of(300.0, "s");
}
