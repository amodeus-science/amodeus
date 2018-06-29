/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class AidoScoreCompiler {

    private final DistanceElement distElem;

    public AidoScoreCompiler(List<RoboTaxi> roboTaxis) {
        distElem = new DistanceElement(roboTaxis.size(), 120000 / 10); // TODO so far set a posteriori, replace hardcoded value

    }

    /** @param roboTaxis
     * @param requests
     * @return current score {mean waiting time, share of full distance, number of taxis} */
    public Tensor compile(long time, List<RoboTaxi> roboTaxis, //
            Collection<AVRequest> requests) {

        Tensor score = Tensors.empty();

        /** the first scalar entry of the score is the mean waiting time at the time instant */
        Tensor waitingTimes = Tensor.of(requests.stream().map(r -> RealScalar.of(time - r.getSubmissionTime())));
        score.append(Tensors.isEmpty(waitingTimes) ? RealScalar.ZERO : (Scalar) Mean.of(waitingTimes));

        /** the second scalar entry of the score is the current distance ratio, i.e. the share of empty miles driven
         * in the current time step */
        SimulationObjectCompiler soc = SimulationObjectCompiler.create(time, "inser empty as unused", -1);
        soc.insertVehicles(roboTaxis);
        distElem.register(soc.compile());
//        distElem.consolidate();
        Scalar distCst = distElem.getNewestDistances().Get(1);
        Scalar distTot = distElem.getNewestDistances().Get(0);

        Scalar score2 = Scalars.lessThan(RealScalar.ZERO, distTot) ? //
                (distTot.subtract(distCst)).divide(distTot) : RealScalar.ZERO;

        System.out.println("distCst: " + distCst);
        System.out.println("distTot: " + distTot);
        System.out.println("score2: " + score2);

        score.append(score2);

        /** the third scalar entry of the score is the fleet size */
        score.append(RationalScalar.of(roboTaxis.size(), 1));

        return score;

    }

}
