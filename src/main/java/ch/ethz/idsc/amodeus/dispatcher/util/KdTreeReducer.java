package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public class KdTreeReducer {

    /** original data */
    private final Collection<RoboTaxi> roboTaxiFull;
    private final Collection<AVRequest> requestsFull;
    private final Network network;
    private final Tensor infoLine;

    /** reduced collections */
    private Collection<RoboTaxi> roboTaxisReduced;
    private Collection<AVRequest> requestsReduced;

    public KdTreeReducer(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunction, Network network, Tensor infoLine) {

        if (!(distanceFunction instanceof EuclideanDistanceFunction)) {
            System.err.println("cannot use requestReducing technique with other distance function than Euclidean.");
            GlobalAssert.that(false);
        }

        this.roboTaxiFull = roboTaxis;
        this.requestsFull = requests;
        this.network = network;
        this.infoLine = infoLine;

        reduce();
    }

    private void reduce() {
        /** append initial problem size to infoLine */
        infoLine.append(Tensors.vectorInt(roboTaxiFull.size(), requestsFull.size()));

        /** reduce the number of roboTaxis */
        roboTaxisReduced = KdTreeReducerHelper.reduceRoboTaxis(requestsFull, roboTaxiFull, network);

        /** reduce the number of requests */
        requestsReduced = KdTreeReducerHelper.reduceRequests(requestsFull, roboTaxiFull, network);

        /** append reduced problem size*/
        infoLine.append(Tensors.vectorInt(roboTaxisReduced.size(), requestsReduced.size()));
    }

    public Collection<RoboTaxi> getReducedRoboTaxis() {
        return roboTaxisReduced;
    }

    public Collection<AVRequest> getReducedRequests() {
        return requestsReduced;
    }

    public Tensor getInfoLine() {
        return infoLine;
    }

}
