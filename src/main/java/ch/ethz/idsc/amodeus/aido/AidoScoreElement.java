/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisElement;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.ResourceData;
import ch.ethz.idsc.tensor.io.TableBuilder;
import ch.ethz.idsc.tensor.qty.Quantity;

public class AidoScoreElement implements AnalysisElement {

    private final AidoDistanceRecorder aidoDistanceRecorder;
    private final TableBuilder scoreDiffTable = new TableBuilder();
    private final TableBuilder scoreIntgTable = new TableBuilder();
    private final Properties scoreparam = ResourceData.properties("/aido/scoreparam.properties");
    private final ServiceQualityScore squScore = new ServiceQualityScore(scoreparam);
    private final EfficiencyScore effScore = new EfficiencyScore(scoreparam);
    private final FleetSizeScore fltScore;

    // ---
    private Scalar timeBefore = Quantity.of(0, SI.SECOND);

    public AidoScoreElement(int numberRoboTaxis, int totReq) {
        aidoDistanceRecorder = new AidoDistanceRecorder(numberRoboTaxis);
        fltScore = new FleetSizeScore(scoreparam, totReq, numberRoboTaxis);
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
        Scalar distEmpty = currDistance.Get(1);

        /** update scores with information */
        squScore.update(Tensors.of(currWaitTime, distEmpty));
        effScore.update(Tensors.of(currWaitTime, distEmpty));
        fltScore.update(currWaitTime, time);

        /** add score differences to history */
        scoreDiffTable.appendRow(time, squScore.getScoreDiff(), effScore.getScoreDiff(), fltScore.getScoreDiff());
        scoreIntgTable.appendRow(time, squScore.getScoreIntg(), effScore.getScoreIntg(), fltScore.getScoreIntg());

        timeBefore = time;
    }

    /** @return incremental score */
    public Tensor getScoreDiff() {
        return Tensors.of(squScore.getScoreDiff(), effScore.getScoreDiff(), fltScore.getScoreDiff());
    }

    public Tensor getScoreDiffHistory() {
        return scoreDiffTable.toTable();
    }

    public Tensor getScoreIntgHistory() {
        return scoreIntgTable.toTable();
    }
}
