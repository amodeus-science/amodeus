/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class AidoScoreCompiler {

    private final AidoDistanceRecorder recorder;
    private Scalar timeBefore = Quantity.of(0, SI.SECOND);
    private Tensor scoreInt = Tensors.of(Quantity.of(0, SI.SECOND), Quantity.of(0, SI.METER), Quantity.of(0, SI.METER));

    public AidoScoreCompiler(List<RoboTaxi> roboTaxis) {
        recorder = new AidoDistanceRecorder(roboTaxis.size());
    }

    public Tensor compile(long timeMatsim, List<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
        Scalar time = Quantity.of(timeMatsim, SI.SECOND);

        /** the first scalar entry of the score is time spent waiting in the current time step */
        Scalar dt = time.subtract(timeBefore);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), time));
        Scalar currWaitTime = RationalScalar.of(requests.size(), 1).multiply(dt);

        /** the second scalar entry of the score is the distance driven with full vehicles (with customer)
         * and the third scalar entry of the score is the distance driven with empty vehicles (without customer)
         * 
         * This distance is always accounted when a vehicle leaves a link, so for an individual vehicle
         * it produced a sequence {...,0,0,d1,0,0,d2,0,...} */
        SimulationObjectCompiler soc = SimulationObjectCompiler.create(timeMatsim, "insert empty as unused", -1);
        soc.insertVehicles(roboTaxis);
        Tensor currDistance = recorder.register(soc.compile());
        Scalar distCusto = currDistance.Get(0);
        Scalar distEmpty = currDistance.Get(1);

        timeBefore = time;
        Tensor scoreAdd = Tensors.of(currWaitTime, distCusto, distEmpty);
        scoreInt = scoreInt.add(scoreAdd);
        return scoreInt;
    }

}
