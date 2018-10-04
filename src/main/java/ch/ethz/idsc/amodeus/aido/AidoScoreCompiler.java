/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class AidoScoreCompiler {
    private static final String INFO_LINE = "";
    private static final int TOTAL_MATCHED_REQUESTS = -1;
    // ---
    private final AidoScoreElement aidoScoreElement;
    private final MatsimAmodeusDatabase db;

    public AidoScoreCompiler(List<RoboTaxi> roboTaxis, int totReq, MatsimAmodeusDatabase db) {
        this.db = db;
        aidoScoreElement = new AidoScoreElement(roboTaxis.size(), totReq, ScoreParameters.GLOBAL, db);
    }

    public Tensor compile(long timeMatsim, List<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
        /** create a {@link SimulationObject} */
        SimulationObjectCompiler simulationObjectCompiler = //
                SimulationObjectCompiler.create(timeMatsim, INFO_LINE, TOTAL_MATCHED_REQUESTS, db);
        simulationObjectCompiler.insertVehicles(roboTaxis);
        simulationObjectCompiler.insertRequests(requests, RequestStatus.EMPTY); // request status not used

        /** insert and evaluate */
        aidoScoreElement.register(simulationObjectCompiler.compile());
        return aidoScoreElement.getScoreDiff();
    }
}
