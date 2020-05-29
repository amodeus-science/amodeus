package org.matsim.amodeus.components.dispatcher.single_heuristic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.components.dispatcher.AVVehicleAssignmentEvent;
import org.matsim.amodeus.components.dispatcher.utils.SingleRideAppender;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskType;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

public class SingleHeuristicDispatcher implements AmodeusDispatcher {
    public final static String TYPE = "SingleHeuristic";

    private boolean reoptimize = true;
    private double nextReplanningTimestamp = 0.0;

    final private SingleRideAppender appender;
    final private String mode;
    final private EventsManager eventsManager;
    final private double replanningInterval;

    final private List<DvrpVehicle> availableVehicles = new LinkedList<>();
    final private List<PassengerRequest> pendingRequests = new LinkedList<>();

    final private QuadTree<DvrpVehicle> availableVehiclesTree;
    final private QuadTree<PassengerRequest> pendingRequestsTree;

    final private Map<DvrpVehicle, Link> vehicleLinks = new HashMap<>();
    final private Map<PassengerRequest, Link> requestLinks = new HashMap<>();

    public enum HeuristicMode {
        OVERSUPPLY, UNDERSUPPLY
    }

    private HeuristicMode dispatcherMode = HeuristicMode.OVERSUPPLY;

    public SingleHeuristicDispatcher(String mode, EventsManager eventsManager, Network network, SingleRideAppender appender, double replanningInterval) {
        this.appender = appender;
        this.mode = mode;
        this.eventsManager = eventsManager;
        this.replanningInterval = replanningInterval;

        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values()); // minx, miny, maxx, maxy

        availableVehiclesTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
        pendingRequestsTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    @Override
    public void onRequestSubmitted(PassengerRequest request) {
        addRequest(request, request.getFromLink());
    }

    @Override
    public void onNextTaskStarted(DvrpVehicle vehicle) {
        Task task = vehicle.getSchedule().getCurrentTask();
        if (task.getTaskType() == AmodeusTaskType.STAY) {
            addVehicle(vehicle, ((AmodeusStayTask) task).getLink());
        }
    }

    private void reoptimize(double now) {
        HeuristicMode updatedMode = availableVehicles.size() > pendingRequests.size() ? HeuristicMode.OVERSUPPLY : HeuristicMode.UNDERSUPPLY;

        if (!updatedMode.equals(dispatcherMode)) {
            dispatcherMode = updatedMode;
            eventsManager.processEvent(new ModeChangeEvent(dispatcherMode, mode, now));
        }

        while (pendingRequests.size() > 0 && availableVehicles.size() > 0) {
            PassengerRequest request = null;
            DvrpVehicle vehicle = null;

            switch (dispatcherMode) {
            case OVERSUPPLY:
                request = findRequest();
                vehicle = findClosestVehicle(request.getFromLink());
                break;
            case UNDERSUPPLY:
                vehicle = findVehicle();
                request = findClosestRequest(vehicleLinks.get(vehicle));
                break;
            }

            removeRequest(request);
            removeVehicle(vehicle);

            appender.schedule(request, vehicle, now);
        }
    }

    @Override
    public void onNextTimestep(double now) {
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

    private void addRequest(PassengerRequest request, Link link) {
        pendingRequests.add(request);
        pendingRequestsTree.put(link.getCoord().getX(), link.getCoord().getY(), request);
        requestLinks.put(request, link);
        // reoptimize = true;
    }

    private PassengerRequest findRequest() {
        return pendingRequests.get(0);
    }

    private DvrpVehicle findVehicle() {
        return availableVehicles.get(0);
    }

    private DvrpVehicle findClosestVehicle(Link link) {
        Coord coord = link.getCoord();
        return availableVehiclesTree.getClosest(coord.getX(), coord.getY());
    }

    private PassengerRequest findClosestRequest(Link link) {
        Coord coord = link.getCoord();
        return pendingRequestsTree.getClosest(coord.getX(), coord.getY());
    }

    @Override
    public void addVehicle(DvrpVehicle vehicle) {
        eventsManager.processEvent(new AVVehicleAssignmentEvent(mode, vehicle.getId(), 0));
        addVehicle(vehicle, vehicle.getStartLink());
    }

    private void addVehicle(DvrpVehicle vehicle, Link link) {
        availableVehicles.add(vehicle);
        availableVehiclesTree.put(link.getCoord().getX(), link.getCoord().getY(), vehicle);
        vehicleLinks.put(vehicle, link);
        // reoptimize = true;
    }

    private void removeVehicle(DvrpVehicle vehicle) {
        if (!availableVehicles.contains(vehicle)) {
            throw new IllegalStateException();
        }

        availableVehicles.remove(vehicle);
        Coord coord = vehicleLinks.remove(vehicle).getCoord();
        availableVehiclesTree.remove(coord.getX(), coord.getY(), vehicle);
    }

    private void removeRequest(PassengerRequest request) {
        if (!pendingRequests.contains(request)) {
            throw new IllegalStateException();
        }

        pendingRequests.remove(request);
        Coord coord = requestLinks.remove(request).getCoord();
        pendingRequestsTree.remove(coord.getX(), coord.getY(), request);
    }

    static public class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            EventsManager eventsManager = inject.get(EventsManager.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            Network network = inject.getModal(Network.class);

            double replanningInterval = Double.parseDouble(operatorConfig.getDispatcherConfig().getParams().getOrDefault("replanningInterval", "10.0"));

            return new SingleHeuristicDispatcher(operatorConfig.getMode(), eventsManager, network, new SingleRideAppender(operatorConfig.getTimingConfig(), router, travelTime),
                    replanningInterval);
        }
    }
}
