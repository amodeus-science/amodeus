package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisElement;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Pretty;
import ch.ethz.idsc.tensor.qty.Quantity;

public class AidoScoreElement implements AnalysisElement {

    private Scalar timeBefore = Quantity.of(0, SI.SECOND);
    private final AidoDistanceRecorder recorder;
    private Tensor scoreInt = Tensors.of(Quantity.of(0, SI.SECOND), Quantity.of(0, SI.METER), Quantity.of(0, SI.METER));

    private Tensor scoreHist = Tensors.empty();

    public AidoScoreElement(int numberRoboTaxis) {
        recorder = new AidoDistanceRecorder(numberRoboTaxis);
    }

    @Override
    public void register(SimulationObject simulationObject) {

        /** time */
        Scalar time = Quantity.of(simulationObject.now, SI.SECOND);
        Scalar dt = time.subtract(timeBefore);

        /** the first scalar entry of the score is time spent waiting in the current time step, i.e.,
         * total of current waiting times: 4 customers waiting for 1 time step -->
         * 4 time steps of waiting time accumulated */
        Scalar currWaitTime = RationalScalar.of(simulationObject.requests.size(), 1).multiply(dt);

        /** the second scalar entry of the score is the distance driven with full vehicles (with customer)
         * the third scalar entry of the score is the distance driven with empty vehicles (without customer)
         * 
         * This distance is always accounted when a vehicle leaves a link, so for an individual vehicle
         * it produced a sequence {...,0,0,d1,0,0,d2,0,...} */
        recorder.register(simulationObject);
        Tensor currDistance = recorder.getDistance();
        Scalar distCusto = currDistance.Get(0);
        Scalar distEmpty = currDistance.Get(1);

        /** compile score and add to integrated score */
        Tensor scoreAdd = Tensors.of(currWaitTime, distCusto, distEmpty);
        checkIncr(scoreAdd);
        scoreInt = scoreInt.add(scoreAdd);

        /** add to history */
        scoreHist = scoreHist.append(Tensors.of(time, scoreInt.get(0),scoreInt.get(1),scoreInt.get(2)));

        timeBefore = time;
    }

    /** @return integrated score */
    public Tensor getCurrentScore() {
        return scoreInt;
    }

    public Tensor getScoreHistory() {
        return scoreHist;
    }

    /** check that score monotonously increasing with respect to time */
    private void checkIncr(Tensor scoreAdd) {
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), scoreAdd.Get(0)));
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.METER), scoreAdd.Get(1)));
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.METER), scoreAdd.Get(2)));
    }

}
