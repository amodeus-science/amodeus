package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.config.modal.DispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregationEvent;
import ch.ethz.matsim.av.dispatcher.single_heuristic.ModeChangeEvent;
import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.refactoring.schedule.AmodeusStayTask;
import ch.ethz.refactoring.schedule.AmodeusTaskType;

public class MultiODHeuristic implements AVDispatcher {
    public final static String TYPE = "MultiOD";

    private boolean reoptimize = true;
    private double nextReplanningTimestamp = 0.0;

    final private String mode;
    final private EventsManager eventsManager;
    final private double replanningInterval;
    final private long numberOfSeats;

    final private List<AVVehicle> availableVehicles = new LinkedList<>();
    final private List<AggregatedRequest> pendingRequests = new LinkedList<>();
    final private List<AggregatedRequest> assignableRequests = new LinkedList<>();

    final private QuadTree<AVVehicle> availableVehiclesTree;
    final private QuadTree<AggregatedRequest> pendingRequestsTree;

    final private Map<AVVehicle, Link> vehicleLinks = new HashMap<>();
    final private Map<AggregatedRequest, Link> requestLinks = new HashMap<>();

    final private Map<AVVehicle, AggregatedRequest> vehicle2Request = new HashMap<>();

    private SingleHeuristicDispatcher.HeuristicMode dispatcherMode = SingleHeuristicDispatcher.HeuristicMode.OVERSUPPLY;

    final private AggregateRideAppender appender;
    final private FactorTravelTimeEstimator estimator;

    public MultiODHeuristic(String mode, EventsManager eventsManager, Network network, AggregateRideAppender appender, FactorTravelTimeEstimator estimator,
            double replanningInterval, long numberOfSeats) {
        this.mode = mode;
        this.eventsManager = eventsManager;
        this.appender = appender;
        this.estimator = estimator;
        this.replanningInterval = replanningInterval;
        this.numberOfSeats = numberOfSeats;

        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values()); // minx, miny, maxx, maxy

        availableVehiclesTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
        pendingRequestsTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    @Override
    public void onRequestSubmitted(AVRequest request) {
        addRequest(request, request.getFromLink());
    }

    @Override
    public void onNextTaskStarted(AVVehicle vehicle) {
        Task task = vehicle.getSchedule().getCurrentTask();
        if (task.getTaskType() == AmodeusTaskType.PICKUP) {
            assignableRequests.remove(vehicle2Request.remove(vehicle));
        }

        if (task.getTaskType() == AmodeusTaskType.STAY) {
            addVehicle(vehicle, ((AmodeusStayTask) task).getLink());
        }
    }

    private void reoptimize(double now) {
        SingleHeuristicDispatcher.HeuristicMode updatedMode = availableVehicles.size() > pendingRequests.size() ? SingleHeuristicDispatcher.HeuristicMode.OVERSUPPLY
                : SingleHeuristicDispatcher.HeuristicMode.UNDERSUPPLY;

        if (!updatedMode.equals(dispatcherMode)) {
            dispatcherMode = updatedMode;
            eventsManager.processEvent(new ModeChangeEvent(dispatcherMode, mode, now));
        }

        while (pendingRequests.size() > 0 && availableVehicles.size() > 0) {
            AggregatedRequest request = null;
            AVVehicle vehicle = null;

            switch (dispatcherMode) {
            case OVERSUPPLY:
                request = findRequest();
                vehicle = findClosestVehicle(request.getMasterRequest().getFromLink());
                break;
            case UNDERSUPPLY:
                vehicle = findVehicle();
                request = findClosestRequest(vehicleLinks.get(vehicle));
                break;
            }

            removeRequest(request);
            removeVehicle(vehicle);
            vehicle2Request.put(vehicle, request);

            assignableRequests.remove(request); // TODO @sebhoerl IMPORTANT; otherwise REscheduling is necessary!!!
            appender.schedule(request, vehicle, now);
        }
    }

    @Override
    public void onNextTimestep(double now) {
        for (Map.Entry<AVRequest, AVRequest> pair : aggregationMap.entrySet()) {
            eventsManager.processEvent(new AggregationEvent(pair.getValue(), pair.getKey(), now));
        }
        aggregationMap.clear();

        appender.update();

        if (now >= nextReplanningTimestamp) {
            reoptimize = true;
            nextReplanningTimestamp = now + replanningInterval;
        }

        if (reoptimize) {
            reoptimize(now);
            reoptimize = false;
        }
    }

    final private Map<AVRequest, AVRequest> aggregationMap = new HashMap<>();

    private void addRequest(AVRequest request, Link link) {
        AggregatedRequest aggregate = findAggregateRequest(request);

        if (aggregate != null) {
            aggregate.addSlaveRequest(request);
            aggregationMap.put(request, aggregate.getMasterRequest());
        } else {
            aggregate = new AggregatedRequest(request, estimator, numberOfSeats);

            pendingRequests.add(aggregate);
            assignableRequests.add(aggregate);
            requestLinks.put(aggregate, link);
            pendingRequestsTree.put(link.getCoord().getX(), link.getCoord().getY(), aggregate);
            // reoptimize = true;
        }
    }

    private AggregatedRequest findAggregateRequest(AVRequest request) {
        AggregatedRequest bestAggregate = null;
        double bestCost = Double.POSITIVE_INFINITY;

        for (AggregatedRequest candidate : assignableRequests) {
            if (candidate == null)
                throw new IllegalStateException();
            Double cost = candidate.accept(request);

            if (cost != null && cost < bestCost) {
                bestCost = cost;
                bestAggregate = candidate;
            }
        }

        return bestAggregate;
    }

    private AggregatedRequest findRequest() {
        return pendingRequests.get(0);
    }

    private AVVehicle findVehicle() {
        return availableVehicles.get(0);
    }

    private AVVehicle findClosestVehicle(Link link) {
        Coord coord = link.getCoord();
        return availableVehiclesTree.getClosest(coord.getX(), coord.getY());
    }

    private AggregatedRequest findClosestRequest(Link link) {
        Coord coord = link.getCoord();
        return pendingRequestsTree.getClosest(coord.getX(), coord.getY());
    }

    @Override
    public void addVehicle(AVVehicle vehicle) {
        addVehicle(vehicle, vehicle.getStartLink());
        eventsManager.processEvent(new AVVehicleAssignmentEvent(mode, vehicle.getId(), 0));
    }

    private void addVehicle(AVVehicle vehicle, Link link) {
        availableVehicles.add(vehicle);
        availableVehiclesTree.put(link.getCoord().getX(), link.getCoord().getY(), vehicle);
        vehicleLinks.put(vehicle, link);
        // reoptimize = true;
    }

    private void removeVehicle(AVVehicle vehicle) {
        availableVehicles.remove(vehicle);
        Coord coord = vehicleLinks.remove(vehicle).getCoord();
        availableVehiclesTree.remove(coord.getX(), coord.getY(), vehicle);
    }

    private void removeRequest(AggregatedRequest request) {
        pendingRequests.remove(request);
        Coord coord = requestLinks.remove(request).getCoord();
        pendingRequestsTree.remove(coord.getX(), coord.getY(), request);
    }

    // TODO: Effectively, this can become a provider now ...
    static public class Factory implements AVDispatcherFactory {
        @Override
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            EventsManager eventsManager = inject.get(EventsManager.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AVRouter parallelRouter = inject.getModal(AVRouter.class);

            DispatcherConfig dispatcherConfig = operatorConfig.getDispatcherConfig();

            double replanningInterval = Double.parseDouble(dispatcherConfig.getParams().getOrDefault("replanningInterval", "10.0"));
            double threshold = Double.parseDouble(dispatcherConfig.getParams().getOrDefault("maximumTimeRadius", "600.0"));
            long numberOfSeats = Long.parseLong(operatorConfig.getGeneratorConfig().getParams().getOrDefault("numberOfSeats", "4"));

            FactorTravelTimeEstimator estimator = new FactorTravelTimeEstimator(threshold);

            return new MultiODHeuristic(operatorConfig.getMode(), eventsManager, network,
                    new ParallelAggregateRideAppender(operatorConfig.getTimingConfig(), parallelRouter, travelTime, estimator), estimator, replanningInterval, numberOfSeats);
        }
    }
}
