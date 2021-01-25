package org.matsim.amodeus.drt;

import org.matsim.amodeus.dvrp.AmodeusOptimizer;
import org.matsim.amodeus.dvrp.request.AmodeusRequest;
import org.matsim.amodeus.routing.AmodeusRoute;
import org.matsim.amodeus.routing.AmodeusRouteFactory;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import org.matsim.contrib.drt.passenger.DrtRequest;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.tracker.OnlineTrackerListener;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;

public class AmodeusDrtOptimizer implements DrtOptimizer, OnlineTrackerListener {
    private AmodeusOptimizer delegate;

    public AmodeusDrtOptimizer(AmodeusOptimizer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void requestSubmitted(Request request) {
        DrtRequest drtRequest = (DrtRequest) request;

        AmodeusRoute avRoute = new AmodeusRouteFactory().createRoute(drtRequest.getFromLink().getId(), drtRequest.getToLink().getId());
        AmodeusRequest avRequest = new AmodeusRequest(request.getId(), drtRequest.getPassengerId(), drtRequest.getFromLink(), drtRequest.getToLink(),
                drtRequest.getSubmissionTime(), drtRequest.getMode(), avRoute, drtRequest.getLatestStartTime() - drtRequest.getEarliestStartTime(),
                drtRequest.getLatestArrivalTime() - drtRequest.getSubmissionTime());

        delegate.requestSubmitted(avRequest);
    }

    @Override
    public void nextTask(DvrpVehicle vehicle) {
        Schedule schedule = vehicle.getSchedule();

        if (!schedule.getStatus().equals(Schedule.ScheduleStatus.STARTED)) {
            // We remove the first task, which is created by DRT ...
            schedule.removeLastTask();

            // ... and add the one we want for Amodeus.
            schedule.addTask(new DrtStayTask(vehicle.getServiceBeginTime(), Double.POSITIVE_INFINITY, vehicle.getStartLink()));
        }

        delegate.nextTask(vehicle);
    }

    @Override
    public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent e) {
        delegate.notifyMobsimBeforeSimStep(e);
    }

    @Override
    public void vehicleEnteredNextLink(DvrpVehicle vehicle, Link nextLink) {
        delegate.vehicleEnteredNextLink(vehicle, nextLink);
    }
}
