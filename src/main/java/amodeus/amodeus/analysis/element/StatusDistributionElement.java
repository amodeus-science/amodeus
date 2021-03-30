/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import java.util.HashMap;
import java.util.Map;

import amodeus.amodeus.analysis.report.TotalValueAppender;
import amodeus.amodeus.analysis.report.TotalValueIdentifier;
import amodeus.amodeus.analysis.report.TtlValIdent;
import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;
import amodeus.amodeus.net.SimulationObject;
import amodeus.amodeus.util.math.Scalar2Number;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Mean;

public class StatusDistributionElement implements AnalysisElement, TotalValueAppender {

    public final Tensor time = Tensors.empty();
    public final Tensor statusTensor = Tensors.empty();
    public final Tensor occupancyTensor = Tensors.empty();

    // average fraction of time vehicles are occupied
    public Scalar avgOccupancy = RealScalar.of(-1); // initialized to avoid errors in later steps

    @Override // from AnalysisElement
    public void register(SimulationObject simulationObject) {

        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));

        /** Get the Status Distribution per TimeStep */
        Tensor numStatus = StaticHelper.getNumStatus(simulationObject);
        statusTensor.append(numStatus);

        /** Get the Occupancy Ratio per TimeStep */
        Scalar occupancyRatio = numStatus.Get(RoboTaxiStatus.DRIVEWITHCUSTOMER.ordinal()) //
                .divide(RealScalar.of(simulationObject.vehicles.size()));
        occupancyTensor.append(Tensors.vector(simulationObject.now, Scalar2Number.of(occupancyRatio).doubleValue()));
    }

    @Override // from AnalysisElement
    public void consolidate() {
        avgOccupancy = (Scalar) Mean.of(Transpose.of(occupancyTensor).get(1));
    }

    @Override // from TotalValueAppender
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> map = new HashMap<>();
        map.put(TtlValIdent.OCCUPANCYRATIO, String.valueOf(avgOccupancy));
        return map;
    }
}
