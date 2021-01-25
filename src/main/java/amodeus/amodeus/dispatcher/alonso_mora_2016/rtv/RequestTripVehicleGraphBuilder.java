package amodeus.amodeus.dispatcher.alonso_mora_2016.rtv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraParameters;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraRequest;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraTravelFunction;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraTravelFunction.Result;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraVehicle;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.RequestTripEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.Trip;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.TripVehicleEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph.RequestVehicleEdge;

public class RequestTripVehicleGraphBuilder {
    private final AlonsoMoraTravelFunction travelFunction;
    private final AlonsoMoraParameters parameters;

    public RequestTripVehicleGraphBuilder(AlonsoMoraTravelFunction travelFunction, AlonsoMoraParameters parameters) {
        this.travelFunction = travelFunction;
        this.parameters = parameters;
    }

    private final Map<Trip, Integer> tripIndices = new HashMap<>();
    private final List<Trip> tripList = new LinkedList<>();

    // TODO: Avoid this class variable!
    private Trip registerTrip(Set<AlonsoMoraRequest> requests) {
        Trip trip = new Trip(requests, tripList.size());
        Integer index = tripIndices.get(trip);

        if (index != null) {
            return tripList.get(index);
        }

        tripList.add(trip);
        tripIndices.put(trip, trip.getIndex());

        return trip;
    }

    public RequestTripVehicleGraph build(RequestVehicleGraph requestVehicleGraph) {
        Set<RequestTripEdge> requestTripEdges = new HashSet<>();
        Set<TripVehicleEdge> tripVehicleEdges = new HashSet<>();

        List<AlonsoMoraVehicle> vehicles = new LinkedList<>();

        int fleetEdgeCount = 0;

        for (Map.Entry<AlonsoMoraVehicle, List<RequestVehicleEdge>> entry : requestVehicleGraph.getRequestVehicleEdges().entrySet()) {
            if (fleetEdgeCount >= parameters.rtvLimitPerFleet) {
                break;
            }

            AlonsoMoraVehicle vehicle = entry.getKey();
            List<RequestVehicleEdge> vehicleEdges = entry.getValue();

            int vehicleIndex = vehicles.size();
            vehicles.add(vehicle);

            int vehicleCapacity = vehicle.getCapacity();
            int vehicleEdgeCount = 0;

            List<Set<Trip>> vehicleTrips = new ArrayList<>(vehicleCapacity);
            List<AlonsoMoraRequest> vehicleRequests = new LinkedList<>();

            for (int k = 0; k < vehicleCapacity; k++) {
                vehicleTrips.add(new HashSet<>());
            }

            // Trips of length 1

            if (vehicleCapacity > 0) {
                for (RequestVehicleEdge edge : vehicleEdges) {
                    if (fleetEdgeCount >= parameters.rtvLimitPerFleet || vehicleEdgeCount >= parameters.rtvLimitPerVehicle) {
                        break;
                    }

                    Trip trip = registerTrip(Collections.singleton(edge.getRequest()));
                    vehicleTrips.get(0).add(trip);

                    requestTripEdges.add(new RequestTripEdge(edge.getRequest(), trip, trip.getIndex()));
                    tripVehicleEdges.add(new TripVehicleEdge(trip.getIndex(), vehicleIndex, edge.getCost(), edge.getSequence()));
                    vehicleEdgeCount++;
                    fleetEdgeCount++;

                    vehicleRequests.add(edge.getRequest());
                }
            }

            // Trips of length 2

            if (vehicleCapacity > 1) {
                for (int i = 0; i < vehicleRequests.size(); i++) {
                    if (fleetEdgeCount >= parameters.rtvLimitPerFleet || vehicleEdgeCount >= parameters.rtvLimitPerVehicle) {
                        break;
                    }

                    for (int j = i + 1; j < vehicleRequests.size(); j++) {
                        if (fleetEdgeCount >= parameters.rtvLimitPerFleet || vehicleEdgeCount >= parameters.rtvLimitPerVehicle) {
                            break;
                        }

                        AlonsoMoraRequest firstRequest = vehicleRequests.get(i);
                        AlonsoMoraRequest secondRequest = vehicleRequests.get(j);

                        if (requestVehicleGraph.hasEdge(firstRequest, secondRequest)) {
                            Optional<Result> result = travelFunction.calculate(vehicle, Arrays.asList(firstRequest, secondRequest));

                            if (result.isPresent()) {
                                Trip trip = registerTrip(new HashSet<>(Arrays.asList(firstRequest, secondRequest)));
                                vehicleTrips.get(1).add(trip);

                                // System.err.println(String.join(", ", result.get().directives.stream().map(d -> d.toString()).collect(Collectors.toList())));

                                tripVehicleEdges.add(new TripVehicleEdge(trip.getIndex(), vehicleIndex, result.get().cost, result.get().directives));
                                vehicleEdgeCount++;
                                fleetEdgeCount++;

                                requestTripEdges.add(new RequestTripEdge(firstRequest, trip, trip.getIndex()));
                                requestTripEdges.add(new RequestTripEdge(secondRequest, trip, trip.getIndex()));
                            }
                        }
                    }
                }
            }

            // Longer trips
            for (int k = 2; k < vehicleCapacity; k++) {
                if (fleetEdgeCount >= parameters.rtvLimitPerFleet || vehicleEdgeCount >= parameters.rtvLimitPerVehicle) {
                    break;
                }

                List<Trip> previousTrips = new ArrayList<>(vehicleTrips.get(k - 1));

                for (int i = 0; i < previousTrips.size(); i++) {
                    if (fleetEdgeCount >= parameters.rtvLimitPerFleet || vehicleEdgeCount >= parameters.rtvLimitPerVehicle) {
                        break;
                    }

                    for (int j = i + 1; j < previousTrips.size(); j++) {
                        if (fleetEdgeCount >= parameters.rtvLimitPerFleet || vehicleEdgeCount >= parameters.rtvLimitPerVehicle) {
                            break;
                        }

                        Trip firstTrip = previousTrips.get(i);
                        Trip secondTrip = previousTrips.get(j);

                        Set<AlonsoMoraRequest> combinedRequests = new HashSet<>();
                        combinedRequests.addAll(firstTrip.getRequests());
                        combinedRequests.addAll(secondTrip.getRequests());

                        Trip unassignedTrip = new Trip(combinedRequests, -1);

                        if (combinedRequests.size() == k + 1) {
                            if (!vehicleTrips.get(k).contains(unassignedTrip) && checkSubtrips(combinedRequests, previousTrips)) {
                                Optional<Result> result = travelFunction.calculate(vehicle, combinedRequests);

                                if (result.isPresent()) {
                                    Trip trip = registerTrip(combinedRequests);
                                    vehicleTrips.get(k).add(trip);

                                    tripVehicleEdges.add(new TripVehicleEdge(trip.getIndex(), vehicleIndex, result.get().cost, result.get().directives));
                                    vehicleEdgeCount++;
                                    fleetEdgeCount++;

                                    for (AlonsoMoraRequest request : combinedRequests) {
                                        requestTripEdges.add(new RequestTripEdge(request, trip, trip.getIndex()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return new RequestTripVehicleGraph(tripList, vehicles, tripVehicleEdges, requestTripEdges);
    }

    private boolean checkSubtrips(Collection<AlonsoMoraRequest> requests, List<Trip> previousTrips) {
        List<AlonsoMoraRequest> requestsList = new ArrayList<>(requests);

        for (int i = 0; i < requestsList.size(); i++) {
            List<AlonsoMoraRequest> residual = new ArrayList<>(requestsList);
            residual.remove(i);

            Trip trip = new Trip(new HashSet<>(residual), 0);

            if (!previousTrips.contains(trip)) {
                return false;
            }
        }

        return true;
    }
}
