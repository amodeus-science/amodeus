package ch.ethz.matsim.av.dispatcher.single_heuristic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.dispatcher.utils.SingleRideAppender;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.refactoring.schedule.AmodeusStayTask;
import ch.ethz.refactoring.schedule.AmodeusTaskType;

public class SingleHeuristicDispatcher implements AVDispatcher {
    public final static String TYPE = "SingleHeuristic";

    private boolean reoptimize = true;
    private double nextReplanningTimestamp = 0.0;

    final private SingleRideAppender appender;
    final private Id<AVOperator> operatorId;
    final private EventsManager eventsManager;
    final private double replanningInterval;

    final private List<AVVehicle> availableVehicles = new LinkedList<>();
    final private List<AVRequest> pendingRequests = new LinkedList<>();

    final private QuadTree<AVVehicle> availableVehiclesTree;
    final private QuadTree<AVRequest> pendingRequestsTree;

    final private Map<AVVehicle, Link> vehicleLinks = new HashMap<>();
    final private Map<AVRequest, Link> requestLinks = new HashMap<>();

    public enum HeuristicMode {
        OVERSUPPLY, UNDERSUPPLY
    }

    private HeuristicMode mode = HeuristicMode.OVERSUPPLY;

    public SingleHeuristicDispatcher(Id<AVOperator> operatorId, EventsManager eventsManager, Network network, SingleRideAppender appender, double replanningInterval) {
        this.appender = appender;
        this.operatorId = operatorId;
        this.eventsManager = eventsManager;
        this.replanningInterval = replanningInterval;

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
        if (task.getTaskType() == AmodeusTaskType.STAY) {
            addVehicle(vehicle, ((AmodeusStayTask) task).getLink());
        }
    }

    private void reoptimize(double now) {
        HeuristicMode updatedMode = availableVehicles.size() > pendingRequests.size() ? HeuristicMode.OVERSUPPLY : HeuristicMode.UNDERSUPPLY;

        if (!updatedMode.equals(mode)) {
            mode = updatedMode;
            eventsManager.processEvent(new ModeChangeEvent(mode, operatorId, now));
        }

        while (pendingRequests.size() > 0 && availableVehicles.size() > 0) {
            AVRequest request = null;
            AVVehicle vehicle = null;

            switch (mode) {
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

    private void addRequest(AVRequest request, Link link) {
        pendingRequests.add(request);
        pendingRequestsTree.put(link.getCoord().getX(), link.getCoord().getY(), request);
        requestLinks.put(request, link);
        // reoptimize = true;
    }

    private AVRequest findRequest() {
        return pendingRequests.get(0);
    }

    private AVVehicle findVehicle() {
        return availableVehicles.get(0);
    }

    private AVVehicle findClosestVehicle(Link link) {
        Coord coord = link.getCoord();
        return availableVehiclesTree.getClosest(coord.getX(), coord.getY());
    }

    private AVRequest findClosestRequest(Link link) {
        Coord coord = link.getCoord();
        return pendingRequestsTree.getClosest(coord.getX(), coord.getY());
    }

    @Override
    public void addVehicle(AVVehicle vehicle) {
        eventsManager.processEvent(new AVVehicleAssignmentEvent(operatorId, vehicle.getId(), 0));
        addVehicle(vehicle, vehicle.getStartLink());
    }

    private void addVehicle(AVVehicle vehicle, Link link) {
        availableVehicles.add(vehicle);
        availableVehiclesTree.put(link.getCoord().getX(), link.getCoord().getY(), vehicle);
        vehicleLinks.put(vehicle, link);
        // reoptimize = true;
    }

    private void removeVehicle(AVVehicle vehicle) {
        if (!availableVehicles.contains(vehicle)) {
            throw new IllegalStateException();
        }

        availableVehicles.remove(vehicle);
        Coord coord = vehicleLinks.remove(vehicle).getCoord();
        availableVehiclesTree.remove(coord.getX(), coord.getY(), vehicle);
    }

    private void removeRequest(AVRequest request) {
        if (!pendingRequests.contains(request)) {
            throw new IllegalStateException();
        }

        pendingRequests.remove(request);
        Coord coord = requestLinks.remove(request).getCoord();
        pendingRequestsTree.remove(coord.getX(), coord.getY(), request);
    }

    static public class Factory implements AVDispatcherFactory {
        @Override
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            EventsManager eventsManager = inject.get(EventsManager.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            OperatorConfig operatorConfig = inject.getModal(OperatorConfig.class);
            AVRouter router = inject.getModal(AVRouter.class);
            Network network = inject.getModal(Network.class);

            double replanningInterval = Double.parseDouble(operatorConfig.getDispatcherConfig().getParams().getOrDefault("replanningInterval", "10.0"));

            return new SingleHeuristicDispatcher(operatorConfig.getId(), eventsManager, network, new SingleRideAppender(operatorConfig.getTimingConfig(), router, travelTime),
                    replanningInterval);
        }
    }
}
