/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.simobj.SimulationObjectCompiler;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class AidoScoreCompiler {

    private final DistanceElement distanceElement;

    public AidoScoreCompiler(List<RoboTaxi> roboTaxis) {
        // TODO AIDO so far set a posteriori, replace hardcoded value if possible
        distanceElement = new DistanceElement(roboTaxis.size(), 120000 / 10);
    }

    // TODO
    // Wait time Score not mean but number of people waiting times 10 s (time interval)
    // Distance score make two (dist tot, dist cust) which are discontinous and send distance at link change, i.e.,
    // {...,0,0,0,linkLength1,0,0,0,...}
    // property: summierbarkeit
    // Score 3 anpassen.

    /** @param roboTaxis
     * @param requests
     * @return current score {mean waiting time, share of full distance, number of taxis} */
    public Tensor compile(long time, List<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {

        /** the first scalar entry of the score is the mean waiting time at the time instant */
        Tensor waitingTimes = Tensor.of(requests.stream() //
                .map(r -> RealScalar.of(time - r.getSubmissionTime())));
        Scalar score1 = Tensors.isEmpty(waitingTimes) //
                ? RealScalar.ZERO
                : (Scalar) Mean.of(waitingTimes);

        /** the second scalar entry of the score is the current distance ratio,
         * i.e. the share of empty miles driven in the current time step */
        SimulationObjectCompiler soc = SimulationObjectCompiler.create(time, "insert empty as unused", -1);
        soc.insertVehicles(roboTaxis);
        distanceElement.register(soc.compile());
        Scalar distCst = distanceElement.getNewestDistances().Get(1);
        Scalar distTot = distanceElement.getNewestDistances().Get(0);

        Scalar score2 = Scalars.lessThan(RealScalar.ZERO, distTot) //
                ? (distTot.subtract(distCst)).divide(distTot)
                : RealScalar.ZERO;

        /** the third scalar entry of the score is the fleet size */
        Scalar score3 = RealScalar.of(roboTaxis.size());

        return Tensors.of(score1, score2, score3);
    }

}
