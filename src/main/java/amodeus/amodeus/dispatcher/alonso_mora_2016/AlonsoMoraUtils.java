package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.RequestTripEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.Trip;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.TripVehicleEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph.RequestRequestEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph.RequestVehicleEdge;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class AlonsoMoraUtils {
    private AlonsoMoraUtils() {

    }

    static public String printSequence(List<StopDirective> sequence) {
        List<String> output = new LinkedList<>();

        for (StopDirective directive : sequence) {
            output.add(String.format("%s %s", directive.isPickup() ? "Pickup" : "Dropoff", directive.getRequest().getId()));
        }

        return "[" + String.join(" -> ", output) + "]";
    }

    static public void printRequestVehicleGraph(RequestVehicleGraph graph) {
        System.out.println("-- Start RV graph ---");
        System.out.println(" ");

        for (Map.Entry<AlonsoMoraVehicle, List<RequestVehicleEdge>> entry : graph.getRequestVehicleEdges().entrySet()) {
            System.out.println("Vehicle " + entry.getKey().getId());

            for (RequestVehicleEdge edge : entry.getValue()) {
                System.out.println("-- Request " + edge.getRequest().getId());
            }
        }

        for (RequestRequestEdge edge : graph.getRequestRequestEdges()) {
            System.out.println("RR " + edge.getFirstRequest().getId() + " <-> " + edge.getSecondRequest().getId());
        }

        System.out.println(" ");
        System.out.println("--- End RV graph ---");
    }

    static public void printRequestTripVehicleGraph(RequestTripVehicleGraph graph) {
        List<Trip> trips = graph.getTrips();
        List<AlonsoMoraVehicle> vehicles = graph.getVehicles();

        List<TripVehicleEdge> tripVehicleEdges = graph.getTripVehicleEdges();
        Set<RequestTripEdge> requestTripEdges = graph.getRequestTripEdges();

        System.out.println("-- Start RTV graph ---");
        System.out.println(" ");

        for (int tripIndex = 0; tripIndex < trips.size(); tripIndex++) {
            final int finalTripIndex = tripIndex;

            List<String> tripRequests = requestTripEdges.stream().filter(e -> e.getTripIndex() == finalTripIndex).map(e -> e.getRequest().getId().toString())
                    .collect(Collectors.toList());

            List<String> tripVehicles = tripVehicleEdges.stream().filter(e -> e.getTripIndex() == finalTripIndex)
                    .map(e -> vehicles.get(e.getVehicleIndex()).getId().toString() + "(" + e.getCost() + ")").collect(Collectors.toList());

            System.out.println("Trip " + tripIndex);
            System.out.println("  Vehicles " + String.join(", ", tripVehicles));
            System.out.println("  Requests " + String.join(", ", tripRequests));
            System.out.println("  Direct " + String.join(", ", trips.get(tripIndex).getRequests().stream().map(r -> r.getId().toString()).collect(Collectors.toList())));
        }

        System.out.println(" ");
        System.out.println("--- End RTV graph ---");
    }
}
