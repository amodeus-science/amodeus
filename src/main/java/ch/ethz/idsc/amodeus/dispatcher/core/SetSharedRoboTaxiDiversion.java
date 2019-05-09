package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/* package */ enum SetSharedRoboTaxiDiversion {
    ;

    /** For UniversalDispatcher, VehicleMaintainer internal use only. Use
     * {@link UniveralDispatcher.setRoboTaxiPickup} or {@link setRoboTaxiRebalance}
     * from dispatchers. Assigns new destination to vehicle, if vehicle is already
     * located at destination, nothing happens. In one pass of {@redispatch(...)} in
     * {@VehicleMaintainer}, the function setVehicleDiversion(...) may only be
     * invoked once for a single {@RoboTaxi} vehicle
     *
     * @paramsRoboTaxi {@link RoboTaxi} supplied with a getFunction,e.g.,
     *                 {@link this.getDivertableRoboTaxis}
     * @param destination
     *            {@link Link} the {@link RoboTaxi} should be diverted to
     * @param status
     *            {@link} the {@link AVStatus} the {@link RoboTaxi} has after
     *            the diversion, depends if used from {@link setRoboTaxiPickup} or
     *            {@link setRoboTaxiRebalance} */
    /* package */ final static void now(RoboTaxi sRoboTaxi, Link destination, FuturePathFactory futurePathFactory, //
            double now, EventsManager eventsManager, boolean reRoute) {
        GlobalAssert.that(RoboTaxiUtils.hasNextCourse(sRoboTaxi));
        // update Status Of Robo Taxi
        // In Handle

        // update schedule ofsRoboTaxi
        final Schedule schedule = sRoboTaxi.getSchedule();
        Task task = schedule.getCurrentTask(); // <- implies that task is started
        new RoboTaxiTaskAdapter(task) {

            @Override
            public void handle(AVDriveTask avDriveTask) {
                if (reRoute || !avDriveTask.getPath().getToLink().equals(destination)) { // ignore when vehicle is already going
                    FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                            sRoboTaxi.getDivertableLocation(), destination, sRoboTaxi.getDivertableTime());
                    if (reRoute && avDriveTask.getPath().getToLink().equals(destination)) {
                        sRoboTaxi.assignDirective(new DriveVehicleRerouteDirective(futurePathContainer, sRoboTaxi));
                    } else {
                        sRoboTaxi.assignDirective(new DriveVehicleDiversionDirective(sRoboTaxi, destination, futurePathContainer));
                    }
                } else
                    sRoboTaxi.assignDirective(EmptyDirective.INSTANCE);
            }

            @Override
            public void handle(AVStayTask avStayTask) {
                if (!avStayTask.getLink().equals(destination)) { // ignore request where location == target
                    FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                            sRoboTaxi.getDivertableLocation(), destination, sRoboTaxi.getDivertableTime());

                    sRoboTaxi.assignDirective(new SharedGeneralStayDirective(sRoboTaxi, destination, futurePathContainer, now));

                } else {
                    sRoboTaxi.assignDirective(EmptyDirective.INSTANCE);
                    SharedCourse nextCourse = SharedCourseListUtils.getStarterCourse(sRoboTaxi).get();
                    if (nextCourse.getMealType().equals(SharedMealType.REDIRECT)) {
                        GlobalAssert.that(avStayTask.getLink().equals(nextCourse.getLink()));
                        GlobalAssert.that(!sRoboTaxi.getDivertableLocation().equals(nextCourse.getLink()));
                        sRoboTaxi.finishRedirection();
                    } else if (nextCourse.getMealType().equals(SharedMealType.PICKUP)) {
                        GlobalAssert.that(avStayTask.getLink().equals(nextCourse.getLink()));
                        GlobalAssert.that(sRoboTaxi.getDivertableLocation().equals(nextCourse.getLink()));
                        // } else if (nextCourse.getMealType().equals(SharedMealType.DROPOFF)) {
                        // GlobalAssert.that(avStayTask.getLink().equals(nextCourse.getLink()));
                        // GlobalAssert.that(sRoboTaxi.getDivertableLocation().equals(nextCourse.getLink()));
                        // sRoboTaxi.setStatus(RoboTaxiStatus.DRIVEWITHCUSTOMER);
                    }
                }
            }

            @Override
            public void handle(AVPickupTask avPickupTask) {
                GlobalAssert.that(RoboTaxiUtils.hasNextCourse(sRoboTaxi));
                Link nextLink = RoboTaxiUtils.getStarterLink(sRoboTaxi);
                GlobalAssert.that(nextLink == destination);
                handlePickupAndDropoff(sRoboTaxi, task, nextLink, now);
            }

            @Override
            public void handle(AVDropoffTask dropOffTask) {
                GlobalAssert.that(RoboTaxiUtils.hasNextCourse(sRoboTaxi));
                // THIS Would mean the dropoffs of this time Step did not take place. And thus the menu still has dropof
                // as next course (in dropof case)
                // TODO Lukas think about how to test this
                // GlobalAssert.that(!dropOffTimes.containsKey(now));

                if (LastTimeStep.check(dropOffTask, now, SharedUniversalDispatcher.SIMTIMESTEP)) {
                    Link nextLink = RoboTaxiUtils.getStarterLink(sRoboTaxi);
                    handlePickupAndDropoff(sRoboTaxi, task, nextLink, now);
                }
                // else {
                // Optional<SharedCourse> courseAfterDropoff = RoboTaxiUtils.getSecondCourse(sRoboTaxi);
                // if (courseAfterDropoff.isPresent()) {
                // Link nextLink = courseAfterDropoff.get().getLink();
                // handlePickupAndDropoff(sRoboTaxi, task, nextLink);
                // }
                // }
            }

            private void handlePickupAndDropoff(RoboTaxi sRoboTaxi, Task task, Link nextLink, double now) {
                boolean isOnLastTask = LastTimeStep.check(task, now, SharedUniversalDispatcher.SIMTIMESTEP);
                boolean isSecondLastTaskAndEndsNow = (task.getEndTime() == now && ScheduleUtils.isNextToLastTask(schedule, task));
                GlobalAssert.that(isOnLastTask || isSecondLastTaskAndEndsNow);
                FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                        sRoboTaxi.getDivertableLocation(), nextLink, task.getEndTime());
                sRoboTaxi.assignDirective(new SharedGeneralPickupOrDropoffDiversionDirective(sRoboTaxi, futurePathContainer, now));
            }
        };

        if (sRoboTaxi.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE)) {
            eventsManager.processEvent(RebalanceVehicleEvent.create(now, sRoboTaxi, destination));
        }

        GlobalAssert.that(MaxTwoMoreTasksAfterEndingOne.check(schedule, task, now, SharedUniversalDispatcher.SIMTIMESTEP));
    }

}
