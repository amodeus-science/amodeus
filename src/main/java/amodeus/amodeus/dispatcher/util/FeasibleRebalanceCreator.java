/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RationalScalar;
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

        for (AtomicInteger ai = new AtomicInteger(); ai.get() < dim0; ai.getAndIncrement()) {
            // count number of outgoing vehicles per vNode
            int outgoingVeh = rebalanceInput.get(ai.get()).stream().map(Scalar.class::cast).reduce(Scalar::add).get().number().intValue();
            int availableVehvNode = availableVehicles //
                    .get(availableVehicles.keySet().stream().filter(v -> v.getIndex() == ai.get()).findAny().get()).size();
            // if number of outoing vehicles too small, reduce proportionally
            if (availableVehvNode < outgoingVeh) {
                Tensor newRow = Floor.of(rebalanceInput.get(ai.get()).multiply(RationalScalar.of(availableVehvNode, outgoingVeh)));
                feasibleRebalance.set(newRow, ai.get());
            }
        }
        return feasibleRebalance;
    }
}
