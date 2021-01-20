package amodeus.amodeus.dispatcher.alonso_mora_2016.rv;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraRequest;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraVehicle;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/** TODO: Now we are saving the edges explicitly. However, when we look at the getters only information
 * structured in a certain way is needed from the RV graph. Hence, we can optimize how things are
 * referenced and saved. */
public class RequestVehicleGraph {
    private final Set<RequestVehicleEdge> requestVehicleEdges;
    private final Set<RequestRequestEdge> requestRequestEdges;

    RequestVehicleGraph(Set<RequestVehicleEdge> requestVehicleEdges, Set<RequestRequestEdge> requestRequestEdges) {
        this.requestRequestEdges = requestRequestEdges;
        this.requestVehicleEdges = requestVehicleEdges;
    }

    public Map<AlonsoMoraVehicle, List<RequestVehicleEdge>> getRequestVehicleEdges() {
        Map<AlonsoMoraVehicle, List<RequestVehicleEdge>> result = new HashMap<>();

        for (RequestVehicleEdge edge : requestVehicleEdges) {
            result.computeIfAbsent(edge.getVehicle(), v -> new LinkedList<>());
            result.get(edge.getVehicle()).add(edge);
        }

        return result;
    }

    public Set<RequestRequestEdge> getRequestRequestEdges() {
        return requestRequestEdges;
    }

    public boolean hasEdge(AlonsoMoraRequest firstRequest, AlonsoMoraRequest secondRequest) {
        return requestRequestEdges.contains(new RequestRequestEdge(firstRequest, secondRequest));
    }

    static public class RequestVehicleEdge {
        private final AlonsoMoraRequest request;
        private final AlonsoMoraVehicle vehicle;
        private final double cost;
        private List<StopDirective> sequence;

        RequestVehicleEdge(AlonsoMoraRequest request, AlonsoMoraVehicle vehicle, double cost, List<StopDirective> sequence) {
            this.request = request;
            this.vehicle = vehicle;
            this.cost = cost;
            this.sequence = sequence;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof RequestVehicleEdge) {
                RequestVehicleEdge otherLink = (RequestVehicleEdge) other;
                return request.getId().equals(otherLink.request.getId()) && vehicle.getId().equals(otherLink.vehicle.getId());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return request.hashCode() + 13 * vehicle.hashCode();
        }

        public List<StopDirective> getSequence() {
            return sequence;
        }

        public AlonsoMoraRequest getRequest() {
            return request;
        }

        public AlonsoMoraVehicle getVehicle() {
            return vehicle;
        }

        public double getCost() {
            return cost;
        }
    }

    static public class RequestRequestEdge {
        private final AlonsoMoraRequest firstRequest;
        private final AlonsoMoraRequest secondRequest;

        RequestRequestEdge(AlonsoMoraRequest firstRequest, AlonsoMoraRequest secondRequest) {
            if (firstRequest.getId().compareTo(secondRequest.getId()) > 0) {
                this.firstRequest = firstRequest;
                this.secondRequest = secondRequest;
            } else {
                this.firstRequest = secondRequest;
                this.secondRequest = firstRequest;
            }
        }

        public AlonsoMoraRequest getFirstRequest() {
            return firstRequest;
        }

        public AlonsoMoraRequest getSecondRequest() {
            return secondRequest;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof RequestRequestEdge) {
                RequestRequestEdge otherLink = (RequestRequestEdge) other;
                return firstRequest.getId().equals(otherLink.firstRequest.getId()) && secondRequest.getId().equals(otherLink.secondRequest.getId());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return firstRequest.hashCode() + 13 + secondRequest.hashCode();
        }
    }
}
