/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class StatusDistributionElement implements AnalysisElement {

    public final Tensor time = Tensors.empty();
    public final Tensor statusTensor = Tensors.empty();
    public final Tensor occupancyTensor = Tensors.empty();

    @Override
    public void register(SimulationObject simulationObject) {

        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));

        /** Get the Status Distribution per TimeStep */
        Tensor numStatus = StaticHelper.getNumStatus(simulationObject);
        statusTensor.append(numStatus);

        /** Get the Occupancy Ratio per TimeStep */
        Scalar occupancyRatio = numStatus.Get(RoboTaxiStatus.DRIVEWITHCUSTOMER.ordinal()).//
                divide(RealScalar.of(simulationObject.vehicles.size()));
        occupancyTensor.append(Tensors.vector(simulationObject.now, occupancyRatio.number().doubleValue()));
    }
}
