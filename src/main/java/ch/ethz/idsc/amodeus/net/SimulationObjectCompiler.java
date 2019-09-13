/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class SimulationObjectCompiler {

    private final SimulationObject simulationObject;
    private final Map<String, VehicleContainer> vehicleMap = new HashMap<>();
    private final Map<String, RequestContainer> requestMap = new HashMap<>();
    private final MatsimAmodeusDatabase db;
    private final long now;

    public static SimulationObjectCompiler create(long timeNow, long timePrev, String infoLine, //
            int total_matchedRequests, MatsimAmodeusDatabase db) {
        SimulationObject simulationObject = new SimulationObject();
        simulationObject.iteration = db.getIteration();
        simulationObject.now = timeNow;
        simulationObject.tPrev = timePrev;
        simulationObject.infoLine = infoLine;
        simulationObject.total_matchedRequests = total_matchedRequests;
        return new SimulationObjectCompiler(simulationObject, db, timeNow);
    }

    private SimulationObjectCompiler(SimulationObject simulationObject, //
            MatsimAmodeusDatabase db, long now) {
        GlobalAssert.that(Objects.nonNull(simulationObject));
        this.db = db;
        this.simulationObject = simulationObject;
        this.now = now;
    }

    public void insertRequests(Collection<AVRequest> requests, RequestStatus status) {
        requests.stream().forEach(r -> insertRequest(r, status));
    }

    public void insertRequests(Map<AVRequest, RequestStatus> requestStatuses) {
        for (Entry<AVRequest, RequestStatus> entry : requestStatuses.entrySet()) {
            insertRequest(entry.getKey(), entry.getValue());
        }
    }

    public void insertVehicles(List<RoboTaxi> robotaxis) {
        robotaxis.forEach(this::insertVehicle);
    }

    private void insertRequest(AVRequest avRequest, RequestStatus requestStatus) {
        if (requestMap.containsKey(avRequest.getId().toString())) {
            requestMap.get(avRequest.getId().toString()).addStatus(now, requestStatus);
        } else {
            RequestContainer requestContainer = RequestContainerCompiler.compile(avRequest, db, requestStatus, now);
            requestMap.put(avRequest.getId().toString(), requestContainer);
        }
    }

    private void insertVehicle(RoboTaxi robotaxi) {
        VehicleContainer vehicleContainer = VehicleContainerCompile.using(robotaxi, db);
        final String key = robotaxi.getId().toString();
        vehicleMap.put(key, vehicleContainer);
    }

    public void addRequestRoboTaxiAssoc(Map<AVRequest, RoboTaxi> map) {
        map.entrySet().stream().forEach(e -> {
            if (requestMap.containsKey(e.getKey().getId().toString())) {
                requestMap.get(e.getKey().getId().toString()).//
                addAssociatedVehicle(now, db.getVehicleIndex(e.getValue()));
            }
        });
    }

    public SimulationObject compile() {
        simulationObject.vehicles = vehicleMap.values().stream().collect(Collectors.toList());
        simulationObject.requests = requestMap.values().stream().collect(Collectors.toList());
        return simulationObject;
    }

}
