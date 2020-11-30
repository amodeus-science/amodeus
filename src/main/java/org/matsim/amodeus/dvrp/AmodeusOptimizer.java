package org.matsim.amodeus.dvrp;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.dvrp.request.AmodeusRequest;
import org.matsim.amodeus.dvrp.request.AmodeusRequestEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.OnlineTrackerListener;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import com.google.inject.Singleton;

@Singleton
public class AmodeusOptimizer implements VrpOptimizer, OnlineTrackerListener, MobsimBeforeSimStepListener {
    private double now;

    private EventsManager eventsManager;
    private AmodeusDispatcher dispatcher;

    public AmodeusOptimizer(AmodeusDispatcher dispatcher, EventsManager eventsManager) {
        this.eventsManager = eventsManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void requestSubmitted(Request request) {
        AmodeusRequest dataRequest = (AmodeusRequest) request;

        synchronized (dispatcher) {
            AmodeusRequestEvent requestEvent = AmodeusRequestEvent.fromRequest(now, dataRequest);
            eventsManager.processEvent(requestEvent);

            dispatcher.onRequestSubmitted(dataRequest);
        }
    }

    private void prepareFirstTask(DvrpVehicle vehicle, Schedule schedule) {
        if (schedule.getTaskCount() != 1) {
            throw new IllegalStateException("Amodeus vehicle schedule should be empty initially.");
        }

        schedule.nextTask();
    }

    private void ensureNonFinishingSchedule(Schedule schedule) {
        Task lastTask = Schedules.getLastTask(schedule);

        if (!lastTask.getTaskType().equals(DrtStayTask.TYPE)) {
            throw new IllegalStateException("An Amodeus schedule should always end with a STAY task");
        }

        if (!Double.isInfinite(lastTask.getEndTime())) {
            throw new IllegalStateException("An Amodeus schedule should always end at time Infinity");
        }
    }

    private void advanceSchedule(DvrpVehicle vehicle, Schedule schedule) {
        // Vehicle finished activity, so we can end the current task in the schedule.
        Task currentTask = schedule.getCurrentTask();
        currentTask.setEndTime(now);

        if (currentTask == Schedules.getLastTask(schedule)) {
            throw new IllegalStateException("An Amodeus schedule should never end!");
        }

        // Adjust begin and end time of the next tasks
        double startTime = now;
        Task nextTask = Schedules.getNextTask(schedule);

        for (int index = nextTask.getTaskIdx(); index < schedule.getTaskCount(); index++) {
            Task task = schedule.getTasks().get(index);

            if (task.getTaskType().equals(DrtStayTask.TYPE)) {
                // Stay tasks should always end when planned (e.g. pre-planned trips)
                task.setEndTime(Math.max(task.getEndTime(), startTime));
            } else {
                // Other tasks are defined by duration, so we keep the planned duration intact
                task.setEndTime(now + (task.getEndTime() - task.getBeginTime()));
            }

            task.setBeginTime(startTime);
            startTime = task.getEndTime();
        }

        // Make sure schedule does not end and start next task
        ensureNonFinishingSchedule(schedule);
        schedule.nextTask();

        // Notify the dispatcher that a new task has started
        synchronized (dispatcher) {
            dispatcher.onNextTaskStarted(vehicle);
        }
    }

    @Override
    public void nextTask(DvrpVehicle vehicle) {
        Schedule schedule = vehicle.getSchedule();

        if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED) {
            // I) Before the schedule, we need to prepare the first stay task.
            prepareFirstTask(vehicle, schedule);
        } else {
            // II) Advance schedule in running simulation
            advanceSchedule(vehicle, schedule);
        }
    }

    @Override
    public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent e) {
        now = e.getSimulationTime();
        dispatcher.onNextTimestep(now);
    }

    @Override
    public void vehicleEnteredNextLink(DvrpVehicle vehicle, Link nextLink) {

    }
}
