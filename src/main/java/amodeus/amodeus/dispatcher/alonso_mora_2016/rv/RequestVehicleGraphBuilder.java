package amodeus.amodeus.dispatcher.alonso_mora_2016.rv;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraRequest;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraTravelFunction;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraTravelFunction.Result;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraVehicle;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph.RequestRequestEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph.RequestVehicleEdge;

public class RequestVehicleGraphBuilder {
    private final AlonsoMoraTravelFunction travelFunction;

    public RequestVehicleGraphBuilder(AlonsoMoraTravelFunction travelFunction) {
        this.travelFunction = travelFunction;
    }

    public RequestVehicleGraph build(double now, Collection<AlonsoMoraVehicle> vehicles, Collection<AlonsoMoraRequest> requests) {
        // TODO: This can be heavily parallelized!

        Set<RequestVehicleEdge> requestVehicleEdges = new HashSet<>();
        List<AlonsoMoraRequest> requestList = new LinkedList<>();

        for (AlonsoMoraRequest request : requests) {
            boolean assignedOnce = false;

            for (AlonsoMoraVehicle vehicle : vehicles) {
                Optional<Result> result = travelFunction.calculate(vehicle, Collections.singleton(request));

                if (result.isPresent()) {
                    requestVehicleEdges.add(new RequestVehicleEdge(request, vehicle, result.get().cost, result.get().directives));
                    assignedOnce = true;
                }
            }

            if (assignedOnce) {
                requestList.add(request);
            }
        }

        Set<RequestRequestEdge> requestRequestEdges = new HashSet<>();
        // List<AlonsoMoraRequest> requestList = new ArrayList<>(requests);

        for (int i = 0; i < requestList.size(); i++) {
            for (int j = i + 1; j < requestList.size(); j++) {
                AlonsoMoraRequest firstRequest = requestList.get(i);
                AlonsoMoraRequest secondRequest = requestList.get(j);

                Optional<Result> result = travelFunction.calculate(firstRequest, secondRequest);

                if (result.isPresent()) {
                    requestRequestEdges.add(new RequestRequestEdge(firstRequest, secondRequest));
                }
            }
        }

        return new RequestVehicleGraph(requestVehicleEdges, requestRequestEdges);
    }
}
