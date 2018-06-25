/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.UnitCapRoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class SimulationObjectCompiler {

    public static SimulationObjectCompiler create( //
            long now, String infoLine, int total_matchedRequests) {
        final MatsimStaticDatabase db = MatsimStaticDatabase.INSTANCE;
        SimulationObject simulationObject = new SimulationObject();
        simulationObject.iteration = db.getIteration();
        simulationObject.now = now;
        simulationObject.infoLine = infoLine;
        simulationObject.total_matchedRequests = total_matchedRequests;
        return new SimulationObjectCompiler(simulationObject);
    }

    private final SimulationObject simulationObject;
    private final Map<String, VehicleContainer> vehicleMap = new HashMap<>();
    private final MatsimStaticDatabase db = MatsimStaticDatabase.INSTANCE;

    private SimulationObjectCompiler(SimulationObject simulationObject) {
        GlobalAssert.that(Objects.nonNull(simulationObject));
        this.simulationObject = simulationObject;
    }

    public void insertRequests(Map<AVRequest, UnitCapRoboTaxi> requestRegister, Map<Id<Vehicle>, RoboTaxiStatus> oldRoboTaxis) {
        for (Entry<AVRequest, UnitCapRoboTaxi> entry : requestRegister.entrySet()) {
            if (Objects.nonNull(entry.getValue())) {
                if (oldRoboTaxis.containsKey(entry.getValue().getId())) {
                    RoboTaxiStatus oldStatus = oldRoboTaxis.get(entry.getValue().getId());
                    RoboTaxiStatus newStatus = entry.getValue().getStatus();
                    insertRequest(entry.getKey(), parseRequestStatus(oldStatus, newStatus));
                } else
                    insertRequest(entry.getKey(), RequestStatus.ASSIGNED);
            } else
                insertRequest(entry.getKey(), RequestStatus.REQUESTED);
        }
    }

    public void insertFulfilledRequests(Map<AVRequest, UnitCapRoboTaxi> requestRegister) {
        requestRegister.forEach((a, r) -> insertRequest(a, RequestStatus.DROPOFF));
    }

    public void insertVehicles(List<UnitCapRoboTaxi> robotaxis) {
        robotaxis.forEach(this::insertVehicle);
    }

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

    private void insertVehicle(UnitCapRoboTaxi robotaxi) {
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
