/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.Floor;

public class Rounder {
    private final Tensor rebalancingCounter;

    public Rounder(VirtualNetwork<Link> virtualNetwork) {
        rebalancingCounter = Array.zeros(virtualNetwork.getvNodesCount(), virtualNetwork.getvNodesCount());
    }

    public void addContribution(VirtualNode<Link> from, VirtualNode<Link> to, Scalar contribution) {
        int fromInd = from.getIndex();
        int toInd = to.getIndex();
        Scalar current = rebalancingCounter.Get(fromInd, toInd);
        Scalar updated = current.add(contribution);
        rebalancingCounter.set(updated, fromInd, toInd);
    }

    public Scalar removeIntegral(VirtualNode<Link> from, VirtualNode<Link> to) {
        int fromInd = from.getIndex();
        int toInd = to.getIndex();
        Scalar current = rebalancingCounter.Get(fromInd, toInd);
        Scalar integrl = Floor.of(current);
        Scalar updated = current.subtract(integrl);
        rebalancingCounter.set(updated, fromInd, toInd);
        return integrl;
    }
}
