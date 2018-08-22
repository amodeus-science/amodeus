/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisElement;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.TableBuilder;
import ch.ethz.idsc.tensor.qty.Quantity;

public class AidoScoreElement implements AnalysisElement {

    private final AidoDistanceRecorder aidoDistanceRecorder;
    private final TableBuilder tableBuilder = new TableBuilder();
    // ---
    private Scalar timeBefore = Quantity.of(0, SI.SECOND);
    private Tensor scoreInt = Tensors.of(Quantity.of(0, SI.SECOND), Quantity.of(0, SI.METER), Quantity.of(0, SI.METER));

    public AidoScoreElement(int numberRoboTaxis) {
        aidoDistanceRecorder = new AidoDistanceRecorder(numberRoboTaxis);
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

        Tensor currDistance = aidoDistanceRecorder.distance(simulationObject);
        Scalar distCusto = currDistance.Get(0);
        Scalar distEmpty = currDistance.Get(1);

        /** compile score and add to integrated score */
        Tensor scoreAdd = Tensors.of(currWaitTime, distCusto, distEmpty);
        StaticHelper.requirePositiveOrZero(scoreAdd);
        scoreInt = scoreInt.add(scoreAdd);

        /** add to history */
        tableBuilder.appendRow(time, scoreInt);

        timeBefore = time;
    }

    /** @return integrated score */
    public Tensor getCurrentScore() {
        return scoreInt.copy();
    }

    public Tensor getScoreHistory() {
        return tableBuilder.toTable();
    }
}
