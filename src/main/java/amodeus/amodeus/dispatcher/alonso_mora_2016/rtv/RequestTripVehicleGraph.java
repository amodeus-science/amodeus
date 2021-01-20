package amodeus.amodeus.dispatcher.alonso_mora_2016.rtv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.optimizer.Request;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraRequest;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraVehicle;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class RequestTripVehicleGraph {
    private final List<TripVehicleEdge> tripVehicleEdges;
    private final Set<RequestTripEdge> requestTripEdges;

    private final List<Trip> trips;
    private final List<AlonsoMoraVehicle> vehicles;

    public RequestTripVehicleGraph(List<Trip> trips, List<AlonsoMoraVehicle> vehicles, Set<TripVehicleEdge> tripVehicleEdges, Set<RequestTripEdge> requestTripEdges) {
        this.tripVehicleEdges = new ArrayList<>(tripVehicleEdges);
        this.requestTripEdges = requestTripEdges;
        this.trips = trips;
        this.vehicles = vehicles;
    }

    public List<TripVehicleEdge> getTripVehicleEdges() {
        return tripVehicleEdges;
    }

    public Set<RequestTripEdge> getRequestTripEdges() {
        return requestTripEdges;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public List<AlonsoMoraVehicle> getVehicles() {
        return vehicles;
    }

    public List<RequestTripEdge> getRequestTripEdges(AlonsoMoraRequest request) {
        return requestTripEdges.stream().filter(e -> e.request.equals(request)).collect(Collectors.toList());
    }

    static public class TripVehicleEdge {
        private final double cost;
        private final int tripIndex;
        private final int vehicleIndex;
        private final List<StopDirective> sequence;

        TripVehicleEdge(int tripIndex, int vehicleIndex, double cost, List<StopDirective> sequence) {
            this.tripIndex = tripIndex;
            this.vehicleIndex = vehicleIndex;
            this.cost = cost;
            this.sequence = sequence;
        }

        public int getTripIndex() {
            return tripIndex;
        }

        public int getVehicleIndex() {
            return vehicleIndex;
        }

        public double getCost() {
            return cost;
        }

        public List<StopDirective> getSequence() {
            return sequence;
        }

        @Override
        public int hashCode() {
            return vehicleIndex * 13 + tripIndex * 1027;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof TripVehicleEdge) {
                TripVehicleEdge otherEdge = (TripVehicleEdge) other;
                return vehicleIndex == otherEdge.vehicleIndex && tripIndex == otherEdge.tripIndex;
            }

            return false;
        }
    }

    static public class RequestTripEdge {
        private final AlonsoMoraRequest request;

        private final double cost;
        private final int tripIndex;

        RequestTripEdge(AlonsoMoraRequest request, Trip trip, double cost, int tripIndex) {
            this.request = request;
            this.cost = cost;
            this.tripIndex = tripIndex;
        }

        public int getTripIndex() {
            return tripIndex;
        }

        public AlonsoMoraRequest getRequest() {
            return request;
        }

        @Override
        public int hashCode() {
            return request.hashCode() * 13 + tripIndex * 1027;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof RequestTripEdge) {
                RequestTripEdge otherEdge = (RequestTripEdge) other;
                return request.equals(otherEdge.request) && tripIndex == otherEdge.tripIndex;
            }

            return false;
        }
    }

    static public class Trip {
        private final Collection<AlonsoMoraRequest> requests = new HashSet<>();
        private final List<Id<Request>> requestIds;
        private final int index;

        Trip(Collection<AlonsoMoraRequest> requests, int index) {
            this.requests.addAll(requests);
            this.index = index;
            this.requestIds = this.requests.stream().map(r -> r.getId()).sorted().collect(Collectors.toList());
        }

        Trip(AlonsoMoraRequest request, int index) {
            this.requests.add(request);
            this.index = index;
            this.requestIds = this.requests.stream().map(r -> r.getId()).sorted().collect(Collectors.toList());
        }

        public Collection<AlonsoMoraRequest> getRequests() {
            return requests;
        }

        int getIndex() {
            return index;
        }

        @Override
        public int hashCode() {
            int hash = 0;

            for (int i = 0; i < requestIds.size(); i++) {
                hash += requestIds.get(i).hashCode();
                hash *= 13;
            }

            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Trip) {
                Trip otherTrip = (Trip) other;

                if (requestIds.size() == otherTrip.requestIds.size()) {
                    for (int i = 0; i < requestIds.size(); i++) {
                        if (!requestIds.get(i).equals(otherTrip.requestIds.get(i))) {
                            return false;
                        }
                    }

                    return true;
                }
            }

            return false;
        }
    }
}
