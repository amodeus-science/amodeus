package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.dvrp.request.AmodeusRequest;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.NoRebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.alonso_mora_2016.ilp.ILPSolver;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rebalancing.RebalancingSolver;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.TripVehicleEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraphBuilder;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraphBuilder;
import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.RebalancingDispatcher;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;
import amodeus.amodeus.net.MatsimAmodeusDatabase;

/** This is a second implementation next to the HighCapacityDispatcher which intends to be a bit more structured and closer to the paper.
 * 
 * @author shoerl */
public class AlonsoMoraDispatcher extends RebalancingDispatcher {
    protected AlonsoMoraDispatcher(Config config, AmodeusModeConfig operatorConfig, TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator,
            EventsManager eventsManager, MatsimAmodeusDatabase db, RebalancingStrategy drtRebalancing, AlonsoMoraParameters parameters, TravelTimeCalculator travelTimeCalculator) {
        super(config, operatorConfig, travelTime, parallelLeastCostPathCalculator, eventsManager, db, drtRebalancing, RoboTaxiUsageType.SHARED);

        this.travelTimeCalculator = travelTimeCalculator;
        this.parameters = parameters;

        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30); // if want to change value, change in av file, here only for backup
        useRebalancing = drtRebalancing instanceof NoRebalancingStrategy;
    }

    private final TravelTimeCalculator travelTimeCalculator;
    private final AlonsoMoraParameters parameters;

    private final IdMap<Request, AlonsoMoraRequest> requests = new IdMap<>(Request.class);

    private final int dispatchPeriod;
    private final boolean useRebalancing;

    @Override
    protected void redispatch(double now) {
        if (now % dispatchPeriod != 0) {
            return;
        }

        // MANAGE REQUESTS

        for (PassengerRequest request : getUnassignedRequests()) {
            if (!requests.containsKey(request.getId())) {
                AmodeusRequest amodeusRequest = (AmodeusRequest) request;

                // TODO: Make travel time calculation based on congestion
                double directTravelTime = travelTimeCalculator.getTravelTime(now, request.getFromLink(), request.getToLink());
                double directDropoffTime = now + directTravelTime;

                double latestPickupTime = now + amodeusRequest.getMaximumWaitTime();
                double latestDropoffTime = now + directTravelTime + amodeusRequest.getMaximumWaitTime() + amodeusRequest.getMaximumDetourTime();

                requests.put(request.getId(), new AlonsoMoraRequest(request, latestPickupTime, latestDropoffTime, directDropoffTime));
            }
        }

        IdSet<Request> maintainedRequestIds = new IdSet<>(Request.class);
        getPassengerRequests().forEach(r -> maintainedRequestIds.add(r.getId()));
        getAssignedRequests().forEach(r -> maintainedRequestIds.add(r.getId()));

        Iterator<AlonsoMoraRequest> iterator = requests.iterator();

        while (iterator.hasNext()) {
            AlonsoMoraRequest request = iterator.next();

            if (!maintainedRequestIds.contains(request.getId())) {
                // Request was either rejected by Amodeus (waiting time exceeded) or dropped off
                iterator.remove();
            }
        }

        // Those are not picked up yet
        Collection<AlonsoMoraRequest> assignmentRequests = getPassengerRequests().stream().map(r -> requests.get(r.getId())).collect(Collectors.toSet());

        // VEHICLES

        List<AlonsoMoraVehicle> vehicles = getDivertableRoboTaxis().stream().map(v -> new DefaultAlonsoMoraVehicle(v)).collect(Collectors.toList());

        // ASSIGNMENT

        AlonsoMoraTravelFunction travelFunction = new DefaultAlonsoMoraTravelFunction(travelTimeCalculator, parameters, requests, pickupDurationPerStop, dropoffDurationPerStop,
                now);

        RequestVehicleGraphBuilder rvBuilder = new RequestVehicleGraphBuilder(travelFunction);
        RequestVehicleGraph rvGraph = rvBuilder.build(now, vehicles, assignmentRequests);

        RequestTripVehicleGraphBuilder rtvBuilder = new RequestTripVehicleGraphBuilder(travelFunction, parameters);
        RequestTripVehicleGraph rtvGraph = rtvBuilder.build(rvGraph);

        ILPSolver ilpSolver = new ILPSolver(parameters);
        Collection<TripVehicleEdge> edges = ilpSolver.solve(rtvGraph, rvGraph);

        IdSet<Request> assignedRequestIds = new IdSet<>(Request.class);
        IdSet<DvrpVehicle> assignedVehicleIds = new IdSet<>(DvrpVehicle.class);

        for (TripVehicleEdge edge : edges) {
            AlonsoMoraVehicle vehicle = rtvGraph.getVehicles().get(edge.getVehicleIndex());
            assignedVehicleIds.add(vehicle.getId());

            List<StopDirective> sequence = edge.getSequence();

            // TODO: Put this into a RequestScheduleCalculator
            double currentTime = now;
            Link currentLink = vehicle.getLocation();

            IdMap<Request, Double> expectedPickupTimes = new IdMap<>(Request.class);

            Set<PassengerRequest> removedRequests = new HashSet<>();

            for (Directive directive : vehicle.getDirectives()) {
                if (directive instanceof StopDirective) {
                    StopDirective stopDirective = (StopDirective) directive;
                    removedRequests.add(stopDirective.getRequest());
                }
            }

            for (StopDirective directive : sequence) {
                currentTime = travelTimeCalculator.getTravelTime(currentTime, currentLink, Directive.getLink(directive));
                currentLink = Directive.getLink(directive);

                AlonsoMoraRequest request = requests.get(directive.getRequest().getId());

                if (directive.isPickup()) {
                    if (parameters.useActivePickupTime) {
                        request.updateActivePickupTime(currentTime);
                    }

                    expectedPickupTimes.put(directive.getRequest().getId(), currentTime);
                } else {
                    if (expectedPickupTimes.containsKey(directive.getRequest().getId())) {
                        double pickupTime = expectedPickupTimes.get(directive.getRequest().getId());
                        addSharedRoboTaxiPickup(vehicle.getVehicle(), directive.getRequest(), pickupTime, currentTime);
                    }

                    assignedRequestIds.add(request.getId());
                    removedRequests.remove(request.getRequest());
                }
            }

            for (PassengerRequest request : removedRequests) {
                abortAvRequest(request);
            }

            // Set directives
            vehicle.getVehicle().getScheduleManager().setDirectives(sequence);
        }

        List<Link> unassignedDestinations = new LinkedList<>();

        for (PassengerRequest request : new HashSet<>(getUnassignedRequests())) {
            // If no match was found, the request should be canceled

            if (isRejectingRequests) {
                cancelRequest(request);
            } else {
                // If we don't want to reject, we make sure we regenerate the AlonsoMoraRequest next round
                requests.remove(request.getId());
            }

            unassignedDestinations.add(request.getFromLink());
        }

        // REBALANCING

        if (useRebalancing) {
            List<AlonsoMoraVehicle> unassignedVehicles = vehicles.stream().filter(v -> v.getVehicle().getOnBoardPassengers() == 0 && !assignedVehicleIds.contains(v.getId()))
                    .collect(Collectors.toList());

            RebalancingSolver rebalancingSolver = new RebalancingSolver(travelTimeCalculator, now);
            Map<AlonsoMoraVehicle, Link> rebalacingTasks = rebalancingSolver.solve(unassignedVehicles, unassignedDestinations);

            for (Map.Entry<AlonsoMoraVehicle, Link> task : rebalacingTasks.entrySet()) {
                AlonsoMoraVehicle vehicle = task.getKey();
                Link destination = task.getValue();

                setRoboTaxiRebalance(vehicle.getVehicle(), destination);
            }
        }
    }

    static public class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);

            AlonsoMoraParameters parameters = new AlonsoMoraParameters();
            parameters.useActivePickupTime = false;
            parameters.useSoftConstraintsAfterAssignment = true;
            parameters.useEuclideanTripProposals = true;

            TravelDisutility travelDisutility = new OnlyTimeDependentTravelDisutility(travelTime);
            LeastCostPathCalculator travelTimeRouter = new DijkstraFactory().createPathCalculator(network, travelDisutility, travelTime);
            TravelTimeCalculator travelTimeCalculator = new DefaultTravelTimeCalculator(travelTimeRouter);

            return new AlonsoMoraDispatcher(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy, parameters, travelTimeCalculator);
        }
    }
}
