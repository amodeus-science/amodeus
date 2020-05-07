package ch.ethz.matsim.av.dispatcher.single_fifo;

import java.util.LinkedList;
import java.util.Queue;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.dispatcher.utils.SingleRideAppender;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.refactoring.schedule.AmodeusTaskType;

public class SingleFIFODispatcher implements AVDispatcher {
    static public final String TYPE = "SingleFIFO";

    final private SingleRideAppender appender;
    final private Queue<AVVehicle> availableVehicles = new LinkedList<>();
    final private Queue<AVRequest> pendingRequests = new LinkedList<>();

    final private EventsManager eventsManager;

    private final Id<AVOperator> operatorId;

    private boolean reoptimize = false;

    public SingleFIFODispatcher(Id<AVOperator> operatorId, EventsManager eventsManager, SingleRideAppender appender) {
        this.appender = appender;
        this.eventsManager = eventsManager;
        this.operatorId = operatorId;
    }

    @Override
    public void onRequestSubmitted(AVRequest request) {
        pendingRequests.add(request);
        reoptimize = true;
    }

    @Override
    public void onNextTaskStarted(AVVehicle vehicle) {
        Task task = vehicle.getSchedule().getCurrentTask();
        if (task.getTaskType() == AmodeusTaskType.STAY) {
            availableVehicles.add(vehicle);
        }
    }

    @Override
    public void addVehicle(AVVehicle vehicle) {
        availableVehicles.add(vehicle);
        eventsManager.processEvent(new AVVehicleAssignmentEvent(operatorId, vehicle.getId(), 0));
    }

    private void reoptimize(double now) {
        while (availableVehicles.size() > 0 && pendingRequests.size() > 0) {
            AVVehicle vehicle = availableVehicles.poll();
            AVRequest request = pendingRequests.poll();
            appender.schedule(request, vehicle, now);
        }

        reoptimize = false;
    }

    @Override
    public void onNextTimestep(double now) {
        appender.update();
        if (reoptimize)
            reoptimize(now);
    }

    static public class Factory implements AVDispatcherFactory {
        @Override
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            EventsManager eventsManager = inject.get(EventsManager.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            OperatorConfig operatorConfig = inject.getModal(OperatorConfig.class);
            AVRouter router = inject.getModal(AVRouter.class);

            return new SingleFIFODispatcher(operatorConfig.getId(), eventsManager, new SingleRideAppender(operatorConfig.getTimingConfig(), router, travelTime));
        }
    }
}
