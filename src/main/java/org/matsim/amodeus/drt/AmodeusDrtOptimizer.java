package org.matsim.amodeus.drt;

import org.matsim.amodeus.dvrp.AVOptimizer;
import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.routing.AVRoute;
import org.matsim.amodeus.routing.AVRouteFactory;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import org.matsim.contrib.drt.passenger.DrtRequest;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.tracker.OnlineTrackerListener;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;

public class AmodeusDrtOptimizer implements DrtOptimizer, OnlineTrackerListener {
    private AVOptimizer delegate;

    public AmodeusDrtOptimizer(AVOptimizer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void requestSubmitted(Request request) {
        DrtRequest drtRequest = (DrtRequest) request;

        AVRoute avRoute = new AVRouteFactory().createRoute(drtRequest.getFromLink().getId(), drtRequest.getToLink().getId());
        AVRequest avRequest = new AVRequest(request.getId(), drtRequest.getPassengerId(), drtRequest.getFromLink(), drtRequest.getToLink(), drtRequest.getSubmissionTime(),
                drtRequest.getMode(), avRoute);

        delegate.requestSubmitted(avRequest);
    }

    @Override
    public void nextTask(DvrpVehicle vehicle) {
        Schedule schedule = vehicle.getSchedule();

        if (!schedule.getStatus().equals(Schedule.ScheduleStatus.STARTED)) {
            // We remove the first task, which is created by DRT ...
            schedule.removeLastTask();

            // ... and add the one we want for Amodeus.
            schedule.addTask(new AmodeusStayTask(vehicle.getServiceBeginTime(), Double.POSITIVE_INFINITY, vehicle.getStartLink()));
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
