/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net.simobj;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class SharedSimulationObjectCompiler {

    public static SharedSimulationObjectCompiler create( //
            long now, String infoLine, int total_matchedRequests) {
        final MatsimStaticDatabase db = MatsimStaticDatabase.INSTANCE;
        SimulationObject simulationObject = new SimulationObject();
        simulationObject.iteration = db.getIteration();
        simulationObject.now = now;
        simulationObject.infoLine = infoLine;
        simulationObject.total_matchedRequests = total_matchedRequests;
        return new SharedSimulationObjectCompiler(simulationObject);
    }

    private final SimulationObject simulationObject;
    private final Map<String, VehicleContainer> vehicleMap = new HashMap<>();
    private final MatsimStaticDatabase db = MatsimStaticDatabase.INSTANCE;

    private SharedSimulationObjectCompiler(SimulationObject simulationObject) {
        GlobalAssert.that(Objects.nonNull(simulationObject));
        this.simulationObject = simulationObject;
    }

    public void insertRequests(Map<AVRequest, RequestStatus> requestStatuses) {
        for (Entry<AVRequest, RequestStatus> entry : requestStatuses.entrySet()) {
            insertRequest(entry.getKey(), entry.getValue());
        }
    }

    public void insertFulfilledRequests(Collection<AVRequest> dropedOffRequests) {
        dropedOffRequests.forEach(a -> insertRequest(a, RequestStatus.DROPOFF));
    }

    public void insertPickedUpRequests(Collection<AVRequest> dropedOffRequests) {
        dropedOffRequests.forEach(a -> insertRequest(a, RequestStatus.PICKUP));
    }

    public void insertVehicles(List<RoboTaxi> robotaxis) {
        robotaxis.forEach(this::insertVehicle);
    }

    // TODO can this be removed and handled in SharedUniversalDispatcher? 
    public RequestStatus parseRequestStatus(RoboTaxiStatus oldStatus, RoboTaxiStatus newStatus) {
        RequestStatus requestStatus = RequestStatusParser.parseRequestStatus(newStatus, oldStatus);
        if (requestStatus == RequestStatus.REQUESTED)
            requestStatus = RequestStatus.ASSIGNED;
        return requestStatus;
    }

    public SimulationObject compile() {
        simulationObject.vehicles = vehicleMap.values().stream().collect(Collectors.toList());
        return simulationObject;
    }

    private void insertRequest(AVRequest avRequest, RequestStatus requestStatus) {
        GlobalAssert.that(Objects.nonNull(avRequest));

        // In future versions this can be removed, because it will be checked in the AV package already
        GlobalAssert.that(Objects.nonNull(avRequest.getFromLink()));
        GlobalAssert.that(Objects.nonNull(avRequest.getToLink()));

        RequestContainer requestContainer = new RequestContainer();
        requestContainer.requestIndex = db.getRequestIndex(avRequest);
        requestContainer.fromLinkIndex = db.getLinkIndex(avRequest.getFromLink());
        requestContainer.submissionTime = avRequest.getSubmissionTime();
        requestContainer.toLinkIndex = db.getLinkIndex(avRequest.getToLink());
        requestContainer.requestStatus = requestStatus;
        simulationObject.requests.add(requestContainer);
    }

    private void insertVehicle(RoboTaxi robotaxi) {
        VehicleContainer vehicleContainer = new VehicleContainer();
        final String key = robotaxi.getId().toString();
        vehicleContainer.vehicleIndex = db.getVehicleIndex(robotaxi);
        final Link fromLink = robotaxi.getLastKnownLocation();
        GlobalAssert.that(Objects.nonNull(fromLink));
        vehicleContainer.linkIndex = db.getLinkIndex(fromLink);
        vehicleContainer.roboTaxiStatus = robotaxi.getStatus();
        Link toLink = robotaxi.getCurrentDriveDestination();
        vehicleContainer.destinationLinkIndex = db.getLinkIndex(Objects.requireNonNull(toLink));
        vehicleMap.put(key, vehicleContainer);
    }
}
