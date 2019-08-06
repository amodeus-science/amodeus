/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;

public enum PopulationArrivalRate {
    ;

    /** @param requests
     * @param virtualNetwork
     * @param timeInterval
     * @param endTime
     * @return {@link Tensor} with indices k,i,j where the elements are the number of requests from virtual station i to j at time interval k. E.g. (5,1,2)=10
     *         means that 10 requests appear in virtual station i with destination in virtual station j at time interval 5. */
    public static Tensor getVNodeAndInterval(Set<Request> requests, VirtualNetwork<Link> virtualNetwork, //
            int timeIntervalLength, int endTime) {
        GlobalAssert.that(endTime % timeIntervalLength == 0);

        Tensor lambda = Array.zeros(endTime / timeIntervalLength, virtualNetwork.getvNodesCount(), virtualNetwork.getvNodesCount());

        for (Request request : requests) {
            int timeIndex = (int) Math.floor(request.startTime() / timeIntervalLength);
            int vNodeIndexFrom = virtualNetwork.getVirtualNode(request.startLink()).getIndex();
            int vNodeIndexTo = virtualNetwork.getVirtualNode(request.endLink()).getIndex();
            // add customer to matrix
            lambda.set(s -> s.add(RealScalar.ONE), timeIndex, vNodeIndexFrom, vNodeIndexTo);
        }
        return lambda;
    }
}
