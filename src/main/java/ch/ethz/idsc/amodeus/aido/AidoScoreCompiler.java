/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.UnitCapRoboTaxi;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum AidoScoreCompiler {
    ;

    /** @param roboTaxis
     * @param requests
     * @return current score is the mean waiting time in the time step */
    public static Tensor compile(long time, List<UnitCapRoboTaxi> roboTaxis, //
            Collection<AVRequest> requests) {

        Tensor waitingTimes = Tensor.of(requests.stream().map(r -> RealScalar.of(time - r.getSubmissionTime())));
        return Tensors.isEmpty(waitingTimes) ? RealScalar.ZERO : (Scalar) Mean.of(waitingTimes);

    }

}
