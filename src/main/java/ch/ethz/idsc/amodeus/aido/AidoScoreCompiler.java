/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class AidoScoreCompiler {

    private final AidoScoreElement aidoScoreElement;
    private final MatsimStaticDatabase db;

    public AidoScoreCompiler(List<RoboTaxi> roboTaxis, int totReq, MatsimStaticDatabase db) {
        this.db = db;
        aidoScoreElement = new AidoScoreElement(roboTaxis.size(), totReq, ScoreParameters.GLOBAL, db);
    }

    public Tensor compile(long timeMatsim, List<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
        /** create a {@link SimulationObject} */
        SimulationObjectCompiler soc = SimulationObjectCompiler.create(timeMatsim, "insert empty as unused", -1, db);
        soc.insertVehicles(roboTaxis);
        soc.insertRequests(requests, RequestStatus.EMPTY); // request status not used

        /** insert and evaluate */
        aidoScoreElement.register(soc.compile());
        return aidoScoreElement.getScoreDiff();
    }
}
