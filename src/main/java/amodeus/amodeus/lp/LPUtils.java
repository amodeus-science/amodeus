/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.lp;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.util.math.Magnitude;
import amodeus.amodeus.virtualnetwork.core.VirtualLink;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.Round;
import ch.ethz.idsc.tensor.sca.Sign;

/* package */ enum LPUtils {
    ;
    static final Scalar AVERAGE_VEL = Quantity.of(30, "km*h^-1");

    /** Takes the Euclidean distance between the centers of the virtual stations
     * and derives the travel time for a given constant velocity.
     *
     * 
     * @param velocity non-zero
     * @return tensor with travel time between the virtual stations in [s], e.g. output.get(i,j) is the travel
     *         time from virtual station i to j */
    static Tensor getEuclideanTravelTimeBetweenVSCenters(VirtualNetwork<Link> virtualNetwork, Scalar velocity) {
        double velocityMperS = Magnitude.VELOCITY.toDouble(velocity); // in m/s
        int nVNodes = virtualNetwork.getvNodesCount();
        Tensor travelTime = Array.zeros(nVNodes, nVNodes);
        for (VirtualLink<Link> link : virtualNetwork.getVirtualLinks()) {
            int sourceIndex = link.getFrom().getIndex();
            int sinkIndex = link.getTo().getIndex();
            travelTime.set(RealScalar.of(link.getDistance() / velocityMperS), sourceIndex, sinkIndex);
        }
        return travelTime;
    }

    /** @param tensor
     * @return the rounded vector where non-negativity and almost integer elements are required, else an exception is thrown */
    static Tensor getRoundedRequireNonNegative(Tensor tensor) {
        Tensor rounded = getRounded(tensor);
        rounded.flatten(-1).map(Scalar.class::cast).forEach(Sign::requirePositiveOrZero);
        return rounded;
    }

    /** @param tensor
     * @return the rounded vector where almost integer elements are required, else an exception is thrown */
    static Tensor getRounded(Tensor tensor) {
        Tensor rounded = Round.of(tensor);
        Chop._04.requireClose(tensor, rounded);
        return rounded;
    }
}
