/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class SimulationObjectCompiler {

    private final SimulationObject simulationObject;
    private final Map<String, VehicleContainer> vehicleMap = new HashMap<>();
    private final Map<String, RequestContainer> requestMap = new HashMap<>();
    private final MatsimAmodeusDatabase db;

    public static SimulationObjectCompiler create( //
            long now, String infoLine, int total_matchedRequests, //
            MatsimAmodeusDatabase db) {
        SimulationObject simulationObject = new SimulationObject();
        simulationObject.iteration = db.getIteration();
        simulationObject.now = now;
        simulationObject.infoLine = infoLine;
        simulationObject.total_matchedRequests = total_matchedRequests;
        return new SimulationObjectCompiler(simulationObject, db);
    }

    private SimulationObjectCompiler(SimulationObject simulationObject, //
            MatsimAmodeusDatabase db) {
        GlobalAssert.that(Objects.nonNull(simulationObject));
        this.db = db;
        this.simulationObject = simulationObject;
    }

    public void insertRequests(Collection<AVRequest> requests, RequestStatus status) {
        requests.forEach(r -> insertRequest(r, status));
    }

    public void insertRequests(Map<AVRequest, RequestStatus> requestStatuses) {
        requestStatuses.forEach(this::insertRequest);
    }

    public void insertVehicles(List<RoboTaxi> roboTaxis) {
        roboTaxis.forEach(rt -> insertVehicle(rt, Arrays.asList(rt.getLastKnownLocation())));
    }

    public void insertVehicles(Map<RoboTaxi, List<Link>> tempLocationTrace) {
        tempLocationTrace.forEach(this::insertVehicle);
    }

    private void insertRequest(AVRequest avRequest, RequestStatus requestStatus) {
        String id = avRequest.getId().toString();
        if (requestMap.containsKey(id)) {
            requestMap.get(id).requestStatus.add(requestStatus);
        } else {
            RequestContainer requestContainer = RequestContainerCompiler.compile(avRequest, db, requestStatus);
            requestMap.put(id, requestContainer);
        }
    }

    private void insertVehicle(RoboTaxi roboTaxi, List<Link> tempTrace) {
        VehicleContainer vehicleContainer = VehicleContainerCompiler.compile(roboTaxi, tempTrace, db);
        final String key = roboTaxi.getId().toString();
        vehicleMap.put(key, vehicleContainer);
    }

    public void addRequestRoboTaxiAssoc(Map<AVRequest, RoboTaxi> map) {
        map.forEach((k, v) -> {
            String id = k.getId().toString();
            if (requestMap.containsKey(id))
                requestMap.get(id).associatedVehicle = v.getId().index();
        });
    }

    public SimulationObject compile() {
        simulationObject.vehicles = new ArrayList<>(vehicleMap.values());
        simulationObject.requests = new ArrayList<>(requestMap.values());
        return simulationObject;
    }
}
