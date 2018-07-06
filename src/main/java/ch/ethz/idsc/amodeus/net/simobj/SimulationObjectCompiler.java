/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net.simobj;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class SimulationObjectCompiler {

    private final SimulationObject simulationObject;
    private final Map<String, VehicleContainer> vehicleMap = new HashMap<>();
    private final Map<String, RequestContainer> requestMap = new HashMap<>();
    private final MatsimStaticDatabase db = MatsimStaticDatabase.INSTANCE;

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

    private SimulationObjectCompiler(SimulationObject simulationObject) {
        GlobalAssert.that(Objects.nonNull(simulationObject));
        this.simulationObject = simulationObject;
    }

//    public void insertRequests(Map<AVRequest, RoboTaxi> requestRegister, Map<Id<Vehicle>, RoboTaxiStatus> oldRoboTaxis) {
//        for (Entry<AVRequest, RoboTaxi> entry : requestRegister.entrySet()) {
//            if (Objects.nonNull(entry.getValue())) {
//                if (oldRoboTaxis.containsKey(entry.getValue().getId())) {
//                    RoboTaxiStatus oldStatus = oldRoboTaxis.get(entry.getValue().getId());
//                    RoboTaxiStatus newStatus = entry.getValue().getStatus();
//                    insertRequest(entry.getKey(), RequestStatusParser.parseRequestStatusSimobj(oldStatus, newStatus));
//                } else
//                    insertRequest(entry.getKey(), RequestStatus.ASSIGNED);
//            } else
//                insertRequest(entry.getKey(), RequestStatus.REQUESTED);
//        }
//    }

//    public void insertFulfilledRequests(Map<AVRequest, RoboTaxi> requestRegister) {
//        requestRegister.forEach((a, r) -> insertRequest(a, RequestStatus.DROPOFF));
//    }

    public void insertRequests(Collection<AVRequest> requests, RequestStatus status) {
        requests.stream().forEach(r -> insertRequest(r, status));
    }

    public void insertVehicles(List<RoboTaxi> robotaxis) {
        robotaxis.forEach(this::insertVehicle);
    }

    private void insertRequest(AVRequest avRequest, RequestStatus requestStatus) {
        RequestContainer requestContainer = RequestContainerCompiler.compile(avRequest, db, requestStatus);
        requestMap.put(avRequest.getId().toString(), requestContainer);
    }

    private void insertVehicle(RoboTaxi robotaxi) {
        VehicleContainer vehicleContainer = VehicleContainerCompiler.compile(robotaxi, db);
        final String key = robotaxi.getId().toString();
        vehicleMap.put(key, vehicleContainer);
    }

    public SimulationObject compile() {
        simulationObject.vehicles = vehicleMap.values().stream().collect(Collectors.toList());
        simulationObject.requests = requestMap.values().stream().collect(Collectors.toList());
        return simulationObject;
    }

}
