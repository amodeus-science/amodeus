package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.utils.objectattributes.attributable.Attributes;

import amodeus.amodeus.dispatcher.alonso_mora_2016.ilp.ILPSolver;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rebalancing.RebalancingSolver;
import amodeus.amodeus.dispatcher.alonso_mora_2016.routing.DefaultTravelFunction;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.TripVehicleEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraphBuilder;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraphBuilder;
import amodeus.amodeus.dispatcher.alonso_mora_2016.sequence.ExtensiveSequenceGenerator;
import amodeus.amodeus.dispatcher.alonso_mora_2016.sequence.MinimumEuclideanDistanceGenerator;
import amodeus.amodeus.dispatcher.alonso_mora_2016.sequence.SequenceGeneratorFactory;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class DefaultAlonsoMoraTravelFunction implements AlonsoMoraTravelFunction {
    private final TravelTimeCalculator travelTimeCalculator;
    private final double pickupDuration;
    private final double dropoffDuration;
    private final double now;
    private final AlonsoMoraParameters parameters;
    private final IdMap<Request, AlonsoMoraRequest> requests;

    private final SequenceGeneratorFactory generatorFactory;

    public DefaultAlonsoMoraTravelFunction(TravelTimeCalculator travelTimeCalculator, AlonsoMoraParameters parameters, IdMap<Request, AlonsoMoraRequest> requests,
            double pickupDuration, double dropoffDuration, double now) {
        this.travelTimeCalculator = travelTimeCalculator;
        this.parameters = parameters;
        this.requests = requests;
        this.now = now;
        this.pickupDuration = pickupDuration;
        this.dropoffDuration = dropoffDuration;

        if (false) {
            this.generatorFactory = new MinimumEuclideanDistanceGenerator.Factory();
        } else {
            this.generatorFactory = new ExtensiveSequenceGenerator.Factory();
        }
    }

    @Override
    public Optional<Result> calculate(AlonsoMoraVehicle vehicle, Collection<AlonsoMoraRequest> requests) {
        return calculate(requests, Optional.of(vehicle));
    }

    @Override
    public Optional<Result> calculate(AlonsoMoraRequest firstRequest, AlonsoMoraRequest secondRequest) {
        return calculate(Arrays.asList(firstRequest, secondRequest), Optional.empty());
    }

    private Optional<Result> calculate(Collection<AlonsoMoraRequest> requests, Optional<AlonsoMoraVehicle> vehicle) {
        List<StopDirective> initialSequence = new LinkedList<>();
        List<PassengerRequest> existingRequests = new LinkedList<>();

        if (vehicle.isPresent()) {
            Set<Id<Request>> pickupRequestIds = new HashSet<>();

            for (Directive directive : vehicle.get().getDirectives()) {
                if (directive.isModifiable()) {
                    if (directive instanceof StopDirective) {
                        StopDirective stopDirective = (StopDirective) directive;

                        if (stopDirective.isPickup()) {
                            pickupRequestIds.add(stopDirective.getRequest().getId());
                        } else {
                            if (!pickupRequestIds.contains(stopDirective.getRequest().getId())) {
                                // We only keep Dropoff directives that are on board
                                initialSequence.add((StopDirective) directive);
                                existingRequests.add(stopDirective.getRequest());
                            }
                        }
                    }
                }
            }
        }

        List<PassengerRequest> addedRequests = new ArrayList<>(requests.size());
        requests.forEach(r -> addedRequests.add(r.getRequest()));

        List<Link> startLinks = new ArrayList<>(2);

        if (vehicle.isPresent()) {
            startLinks.add(vehicle.get().getLocation());
        } else {
            for (AlonsoMoraRequest request : requests) {
                startLinks.add(request.getRequest().getFromLink());
            }
        }

        double minimumCost = Double.POSITIVE_INFINITY;
        List<StopDirective> minimumCostSequence = null;

        for (Link startLink : startLinks) {
            IdMap<Request, RequestSchedule> initialSchedules = new IdMap<>(Request.class);

            if (parameters.useSoftConstraintsAfterAssignment) {
                initialSchedules = getRequestSchedules(initialSequence, startLink);
            }

            Iterator<List<StopDirective>> generator = generatorFactory.create(startLink, existingRequests, addedRequests);

            while (generator.hasNext()) {
                List<StopDirective> proposedSequence = generator.next();
                IdMap<Request, RequestSchedule> proposedSchedules = getRequestSchedules(proposedSequence, startLink);

                // TODO: Here we may add additional constraints!
                if (verifyTiming(proposedSchedules, initialSchedules)) {
                    double cost = 0.0;

                    for (Map.Entry<Id<Request>, RequestSchedule> entry : proposedSchedules.entrySet()) {
                        AlonsoMoraRequest request = getRequest(entry.getKey());
                        cost += Math.max(0, entry.getValue().dropoffTime - request.getDirectDropoffTime());

                        if (cost > minimumCost) {
                            cost = Double.POSITIVE_INFINITY;
                            break;
                        }
                    }

                    /* System.out.println(AlonsoMoraUtils.printSequence(proposedSequence));
                     * System.out.println(cost);
                     * System.out.println("---"); */

                    if (cost < minimumCost) {
                        minimumCost = cost;
                        minimumCostSequence = proposedSequence;
                    }
                }
            }
        }

        if (minimumCostSequence != null) {
            return Optional.of(new Result(minimumCostSequence, minimumCost));
        } else {
            return Optional.empty();
        }
    }

    private boolean verifyTiming(IdMap<Request, RequestSchedule> proposedSchedules, IdMap<Request, RequestSchedule> initialSchedules) {
        for (Map.Entry<Id<Request>, RequestSchedule> entry : proposedSchedules.entrySet()) {
            RequestSchedule proposedSchedule = entry.getValue();
            RequestSchedule initialSchedule = initialSchedules.get(entry.getKey());

            if (initialSchedule != null && parameters.useSoftConstraintsAfterAssignment) {
                // The request is already assigned to the vehicle. If the vehicle is delayed
                // (due to congestion) we now might completely exclude the request-vehicle combination
                // from the tree, which may not be ideal. If the option is actived in the parameters,
                // we, therefore, only make sure that no *additional* delay due to scheduling is
                // introduced (that means normally new requests can only be appended, but not
                // inserted before the already delayed request).

                if (proposedSchedule.pickupTime > initialSchedule.pickupTime) {
                    return false;
                }

                if (proposedSchedule.dropoffTime > initialSchedule.dropoffTime) {
                    return false;
                }
            } else {
                AlonsoMoraRequest request = getRequest(entry.getKey());

                if (proposedSchedule.pickupTime > request.getActivePickupTime()) {
                    return false;
                }

                if (proposedSchedule.dropoffTime > request.getLatestDropoffTime()) {
                    return false;
                }
            }
        }

        return true;
    }

    private IdMap<Request, RequestSchedule> getRequestSchedules(List<StopDirective> directives, Link startLink) {
        double currentTime = now;
        Link currentLink = startLink;

        IdMap<Request, RequestSchedule> schedules = new IdMap<>(Request.class);

        for (StopDirective stop : directives) {
            RequestSchedule schedule = schedules.computeIfAbsent(stop.getRequest().getId(), id -> new RequestSchedule());

            currentTime += travelTimeCalculator.getTravelTime(currentTime, currentLink, Directive.getLink(stop));
            currentLink = Directive.getLink(stop);

            if (stop.isPickup()) {
                schedule.pickupTime = currentTime;
                currentTime += pickupDuration; // TODO: What about per stop vs per passenger?
            } else {
                schedule.dropoffTime = currentTime;
                currentTime += dropoffDuration; // TODO: What about per stop vs per passenger?
            }
        }

        return schedules;
    }

    // TODO: This is called twice. We can also create a map that is relevant the specific problem sequence and save time for the lookup.
    private AlonsoMoraRequest getRequest(Id<Request> requestId) {
        return Objects.requireNonNull(requests.get(requestId));
    }

    static public class RequestSchedule {
        public double pickupTime = Double.NaN;
        public double dropoffTime = Double.NaN;
    }

    static public void main(String[] args) {
        Link link1 = new MockLink(Id.createLinkId("L1"), 100.0);
        Link link2 = new MockLink(Id.createLinkId("L2"), 200.0);
        Link link3 = new MockLink(Id.createLinkId("L3"), 300.0);
        Link link4 = new MockLink(Id.createLinkId("L4"), 400.0);
        Link link5 = new MockLink(Id.createLinkId("L5"), 500.0);

        MockTravelTimeCalculator travelTimeCalculator = new MockTravelTimeCalculator();
        travelTimeCalculator.addBidirectionalTravelTime(link1, link1, 0.0);
        travelTimeCalculator.addBidirectionalTravelTime(link2, link2, 0.0);
        travelTimeCalculator.addBidirectionalTravelTime(link3, link3, 0.0);
        travelTimeCalculator.addBidirectionalTravelTime(link4, link4, 0.0);
        travelTimeCalculator.addBidirectionalTravelTime(link5, link5, 0.0);

        travelTimeCalculator.addBidirectionalTravelTime(link1, link2, 300.0);
        travelTimeCalculator.addBidirectionalTravelTime(link2, link3, 300.0);
        travelTimeCalculator.addBidirectionalTravelTime(link3, link4, 300.0);
        travelTimeCalculator.addBidirectionalTravelTime(link4, link5, 300.0);

        travelTimeCalculator.addBidirectionalTravelTime(link1, link3, 600.0);
        travelTimeCalculator.addBidirectionalTravelTime(link2, link4, 600.0);
        travelTimeCalculator.addBidirectionalTravelTime(link3, link5, 600.0);

        travelTimeCalculator.addBidirectionalTravelTime(link1, link4, 900.0);
        travelTimeCalculator.addBidirectionalTravelTime(link2, link5, 900.0);

        travelTimeCalculator.addBidirectionalTravelTime(link1, link5, 1200.0);

        // travelTimeCalculator.addUnidirectionalTravelTime(link3, link4, 9000.0);

        PassengerRequest passengerRequest1 = new MockRequest("R1", link2, link4);
        PassengerRequest passengerRequest2 = new MockRequest("R2", link3, link5);

        AlonsoMoraRequest amRequest1 = new AlonsoMoraRequest(passengerRequest1, 36000.0, 36000.0, 600.0);
        AlonsoMoraRequest amRequest2 = new AlonsoMoraRequest(passengerRequest2, 36000.0, 36000.0, 600.0);

        IdMap<Request, AlonsoMoraRequest> requests = new IdMap<>(Request.class);
        requests.put(amRequest1.getRequest().getId(), amRequest1);
        requests.put(amRequest2.getRequest().getId(), amRequest2);

        AlonsoMoraParameters parameters = new AlonsoMoraParameters();

        // AlonsoMoraTravelFunction travelFunction = new DefaultAlonsoMoraTravelFunction(travelTimeCalculator, parameters, requests, 60.0, 60.0, 0.0);
        AlonsoMoraTravelFunction travelFunction = new DefaultTravelFunction(parameters, 0.0, travelTimeCalculator, requests, 60.0, 60.0, Collections.emptySet());

        {
            Optional<Result> result = travelFunction.calculate(amRequest1, amRequest2);
            System.out.println(AlonsoMoraUtils.printSequence(result.get().directives));
            System.out.println(result.get().cost);
        }

        // ---

        MockVehicle vehicle = new MockVehicle("V1", link3);

        {
            Optional<Result> result = travelFunction.calculate(vehicle, Arrays.asList(amRequest1, amRequest2));
            System.out.println(AlonsoMoraUtils.printSequence(result.get().directives));
            System.out.println(result.get().cost);
        }

        vehicle = new MockVehicle("V1", link4, Arrays.asList(Directive.dropoff(passengerRequest2)));

        {
            Optional<Result> result = travelFunction.calculate(vehicle, Arrays.asList(amRequest1));
            System.out.println(AlonsoMoraUtils.printSequence(result.get().directives));
            System.out.println(result.get().cost);
        }

        vehicle = new MockVehicle("V1", link2);
        MockVehicle vehicle2 = new MockVehicle("V2", link3);

        // travelTimeCalculator.addBidirectionalTravelTime(link2, link3, 9300.0);
        // travelTimeCalculator.addBidirectionalTravelTime(link2, link4, 9600.0);
        // travelTimeCalculator.addBidirectionalTravelTime(link2, link5, 9900.0);

        PassengerRequest passengerRequest3 = new MockRequest("R3", link1, link5);
        AlonsoMoraRequest amRequest3 = new AlonsoMoraRequest(passengerRequest3, 36000.0, 36000.0, 600.0);
        requests.put(amRequest3.getRequest().getId(), amRequest3);

        RequestVehicleGraphBuilder rvBuilder = new RequestVehicleGraphBuilder(travelFunction);
        RequestVehicleGraph rvGraph = rvBuilder.build(0.0, Arrays.asList(vehicle, vehicle2), Arrays.asList(amRequest1, amRequest2, amRequest3));

        AlonsoMoraUtils.printRequestVehicleGraph(rvGraph);

        RequestTripVehicleGraphBuilder rtvBuilder = new RequestTripVehicleGraphBuilder(travelFunction, parameters);
        RequestTripVehicleGraph rtvGraph = rtvBuilder.build(rvGraph);

        AlonsoMoraUtils.printRequestTripVehicleGraph(rtvGraph);

        ILPSolver ilpSolver = new ILPSolver(parameters);
        Collection<TripVehicleEdge> edges = ilpSolver.solve(rtvGraph, rvGraph);

        for (TripVehicleEdge edge : edges) {
            System.out.println(rtvGraph.getVehicles().get(edge.getVehicleIndex()).getId() + ": " + AlonsoMoraUtils.printSequence(edge.getSequence()));
        }

        // REBALANCING

        RebalancingSolver rebalancingSolver = new RebalancingSolver(travelTimeCalculator, 0.0);
        Map<AlonsoMoraVehicle, Link> destinations = rebalancingSolver.solve(Arrays.asList(vehicle, vehicle2), Arrays.asList(link1, link2, link5));

        for (Map.Entry<AlonsoMoraVehicle, Link> entry : destinations.entrySet()) {
            System.out.println(entry.getKey().getId() + " -> " + entry.getValue().getId());
        }

    }

    static public class MockLink implements Link {
        private final Id<Link> linkId;
        private final Coord coord;

        public MockLink(Id<Link> linkId, double x) {
            this.linkId = linkId;
            this.coord = new Coord(x, 0.0);
        }

        @Override
        public Coord getCoord() {
            return coord;
        }

        @Override
        public Attributes getAttributes() {
            return null;
        }

        @Override
        public Id<Link> getId() {
            return linkId;
        }

        @Override
        public boolean setFromNode(Node node) {
            return true;
        }

        @Override
        public boolean setToNode(Node node) {
            return true;
        }

        @Override
        public Node getToNode() {
            return null;
        }

        @Override
        public Node getFromNode() {
            return null;
        }

        @Override
        public double getLength() {
            return 0;
        }

        @Override
        public double getNumberOfLanes() {
            return 0;
        }

        @Override
        public double getNumberOfLanes(double time) {
            return 0;
        }

        @Override
        public double getFreespeed() {
            return 0;
        }

        @Override
        public double getFreespeed(double time) {
            return 0;
        }

        @Override
        public double getCapacity() {
            return 0;
        }

        @Override
        public double getCapacity(double time) {
            return 0;
        }

        @Override
        public void setFreespeed(double freespeed) {

        }

        @Override
        public void setLength(double length) {

        }

        @Override
        public void setNumberOfLanes(double lanes) {

        }

        @Override
        public void setCapacity(double capacity) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setAllowedModes(Set<String> modes) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<String> getAllowedModes() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public double getFlowCapacityPerSec() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getFlowCapacityPerSec(double time) {
            // TODO Auto-generated method stub
            return 0;
        }

    }

    static public class MockTravelTimeCalculator implements TravelTimeCalculator {
        private final Map<Tuple<Id<Link>, Id<Link>>, Double> travelTimes = new HashMap<>();

        @Override
        public double getTravelTime(double departureTime, Link originLink, Link destinationLink) {
            return travelTimes.get(new Tuple<>(originLink.getId(), destinationLink.getId()));
        }

        public void addUnidirectionalTravelTime(Link origin, Link destination, double travelTime) {
            travelTimes.put(new Tuple<>(origin.getId(), destination.getId()), travelTime);
        }

        public void addBidirectionalTravelTime(Link origin, Link destination, double travelTime) {
            travelTimes.put(new Tuple<>(origin.getId(), destination.getId()), travelTime);
            travelTimes.put(new Tuple<>(destination.getId(), origin.getId()), travelTime);
        }

        @Override
        public void clear() {
            // TODO Auto-generated method stub
            
        }
    }

    static public class MockRequest implements PassengerRequest {
        private final Id<Request> requestId;
        private final Link originLink;
        private final Link destinationLink;

        public MockRequest(String requestId, Link originLink, Link destinationLink) {
            this.requestId = Id.create(requestId, Request.class);
            this.originLink = originLink;
            this.destinationLink = destinationLink;
        }

        @Override
        public double getSubmissionTime() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Id<Request> getId() {
            return requestId;
        }

        @Override
        public double getEarliestStartTime() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Link getFromLink() {
            return originLink;
        }

        @Override
        public Link getToLink() {
            return destinationLink;
        }

        @Override
        public Id<Person> getPassengerId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getMode() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    static public class MockVehicle implements AlonsoMoraVehicle {
        private final List<Directive> directives;
        private final Link location;
        private final Id<DvrpVehicle> id;

        public MockVehicle(String id, Link location, List<Directive> directives) {
            this.id = Id.create(id, DvrpVehicle.class);
            this.location = location;
            this.directives = directives;
        }

        public MockVehicle(String id, Link location) {
            this(id, location, Arrays.asList());
        }

        @Override
        public Id<DvrpVehicle> getId() {
            return id;
        }

        @Override
        public Link getLocation() {
            return location;
        }

        @Override
        public List<Directive> getDirectives() {
            return directives;
        }

        @Override
        public int getCapacity() {
            return 4;
        }

        @Override
        public RoboTaxi getVehicle() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
