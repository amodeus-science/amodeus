/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.sca.Floor;
import ch.ethz.idsc.tensor.sca.Sign;

public enum FeasibleRebalanceCreator {
    ;
    /** @param rebalanceInput
     * @param availableVehicles
     * @return returns a scaled rebalanceInput which is feasible considering the available number of
     *         vehicles */
    public static Tensor returnFeasibleRebalance(Tensor rebalanceInput, Map<VirtualNode<Link>, //
            List<RoboTaxi>> availableVehicles) {

        int dim0 = Dimensions.of(rebalanceInput).get(0);
        int dim1 = Dimensions.of(rebalanceInput).get(1);

        GlobalAssert.that(dim0 == dim1);
        GlobalAssert.that(dim0 == availableVehicles.size());
        GlobalAssert.that(rebalanceInput.flatten(-1).map(Scalar.class::cast).allMatch(Sign::isPositiveOrZero));

        Tensor feasibleRebalance = rebalanceInput.copy();

        for (int i = 0; i < dim0; ++i) {
            // count number of outgoing vehicles per vNode
            double outgoingNmrvNode = 0.0;
            Tensor outgoingVehicles = rebalanceInput.get(i);
            for (int j = 0; j < dim0; ++j) {
                outgoingNmrvNode = outgoingNmrvNode + outgoingVehicles.Get(j).number().doubleValue();
            }
            int outgoingVeh = (int) outgoingNmrvNode;
            int finalI = i;
            int availableVehvNode = availableVehicles //
                    .get(availableVehicles.keySet().stream().filter(v -> v.getIndex() == finalI).findAny().get()).size();
            // if number of outoing vehicles too small, reduce proportionally
            if (availableVehvNode < outgoingVeh) {
                double shrinkingFactor = ((double) availableVehvNode / ((double) outgoingVeh));
                Tensor newRow = Floor.of(rebalanceInput.get(i).multiply(RealScalar.of(shrinkingFactor)));
                feasibleRebalance.set(newRow, i);
            }
        }
        return feasibleRebalance;
    }
}
