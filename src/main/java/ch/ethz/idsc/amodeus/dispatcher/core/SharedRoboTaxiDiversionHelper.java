/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.schedule.AVTask;
import ch.ethz.matsim.av.schedule.AVTask.AVTaskType;

/*package*/ enum SharedRoboTaxiDiversionHelper {
    ;

    /* package */ static void adaptMenuToDirective(RoboTaxi roboTaxi, FuturePathFactory futurePathFactory, double now, EventsManager eventsManager) {
        // Check that we are not already on the link of the redirectino (this can only happen if a command was given in redispatch to the current location)
        removeRedirectionToDivertableLocationInBeginning(roboTaxi);

        Optional<Link> link = getToLink(roboTaxi, now);
        if (link.isPresent()) {
            setRoboTaxiDiversion(roboTaxi, link.get(), futurePathFactory, now, eventsManager);
        }

    }

    /* package */static boolean maxTwoMoreTaskAfterThisOneWhichEnds(Schedule schedule, Task task, double now, double timeStep) {
        if (thisIsLastTimeStep(task, now, timeStep)) {
            return task.getTaskIdx() >= schedule.getTaskCount() - 3;
        }
        return task.getTaskIdx() >= schedule.getTaskCount() - 2;
    }

    private static boolean thisIsLastTimeStep(Task task, double now, double timeStep) {
        return task.getEndTime() < now + timeStep;
    }

    /* package */ static Optional<Link> getToLink(RoboTaxi roboTaxi, double now) {

        GlobalAssert.that(!nextCourseIsRedirectToCurrentLink(roboTaxi));

        Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
        final Schedule schedule = roboTaxi.getSchedule();
        final Task currentTask = schedule.getCurrentTask();
        boolean isOnLastTask = currentTask == Schedules.getLastTask(schedule);
        boolean isSecondLastTask = ScheduleUtils.isNextToLastTask(schedule, currentTask);
        boolean taskEndsNow = thisIsLastTimeStep(currentTask, now, SharedUniversalDispatcher.SIMTIMESTEP);

        if (currentCourse.isPresent()) {
            if (roboTaxi.isWithoutDirective()) {

                final AVTask avTask = (AVTask) currentTask;
                boolean divert = false;
                // FIRST: We reach the point where the Robo Taxi does not know what to do based on the schedule
                // We have a current Course but the task is close to the end or already on the last task
                if (isOnLastTask || (isSecondLastTask && taskEndsNow)) {
                    divert = true;
                }

                // SECOND: The course Of the Menu Changed
                // SECOND A): IF We are on a Stay Task currently, we should have changed is already in the first step above

                // SECOND B): If We are on a Drive Task currently, we have to see if the planed direction still fits our needs
                if (avTask.getAVTaskType().equals(AVTaskType.DRIVE)) {
                    GlobalAssert.that(isSecondLastTask);
                    Link planedToLink = ((AVDriveTask) avTask).getPath().getToLink();
                    if (!planedToLink.equals(currentCourse.get().getLink())) {
                        if (!planedToLink.equals(roboTaxi.getDivertableLocation())) {
                            divert = true;
                        } else {
                            // TODO remove soon if no errors
                            GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiUtils.calculateStatusFromMenu(roboTaxi)));
                        }
                    } else {
                        // TODO remove soon if no errors
                        GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiUtils.calculateStatusFromMenu(roboTaxi)));
                    }
                    if (planedToLink.equals(roboTaxi.getDivertableLocation())) {
                        if (isOnLastTask) {
                            GlobalAssert.that(divert); // should be set to true as it is second last task and and finished
                        }
                    }
                }

                // SECOND C): If We are on a Pickup or Dropoff Task currently, we should wait until we reach the end of this task as we are not going to abort. But then The
                // First part helps us.

                // THIRD AND FINAL: Divert If Required
                if (divert) {
                    return Optional.of(currentCourse.get().getLink());
                }
            }
        } else {
            // HERE WE MAKE SURE THE STATUS IS SET CORRECT AFTER THE FINISH OF THE LAST TASK
            // FIRST a): if there is no curse and we are on the last task then All is fine as long as we are in Stay Status
            if (isOnLastTask) {
                GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
            } else if (isSecondLastTask && taskEndsNow) {
                // FIRST b): if we will finish the second last task now then the next status will be stay. As we have nothing to do.
                final AVTask avTask = (AVTask) currentTask;
                if (avTask.getAVTaskType().equals(AVTaskType.DRIVE)) {
                    AVDriveTask avDriveTask = (AVDriveTask) avTask;
                    // AS there is no task after this one and the currend destinadtion is not the current location we have to divert the robo taxi to the current location
                    if (!roboTaxi.getDivertableLocation().equals(avDriveTask.getPath().getToLink())) {
                        SharedCourse redirectCourse = SharedCourse.redirectCourse(roboTaxi.getDivertableLocation(),
                                Double.toString(now) + "_currentLink_" + roboTaxi.getId().toString());
                        roboTaxi.addRedirectCourseToMenuAtBegining(redirectCourse);
                        return Optional.of(roboTaxi.getDivertableLocation());
                    }
                    // TODO remove soon if no errors
                    GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
                } else //
                if (avTask.getAVTaskType().equals(AVTaskType.DROPOFF)) {
                    // TODO remove soon if no errors
                    GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
                } else {
                    System.out.println(" Stay Task:" + avTask.getAVTaskType().equals(AVTaskType.STAY));
                    System.out.println("PICKUP TASK" + avTask.getAVTaskType().equals(AVTaskType.PICKUP));
                    GlobalAssert.that(false);
                }
                // SECOND: if we are on the second last task but it does not end yet then we have to stop the current task if thats possible
            } else if (isSecondLastTask) {
                // Lets consider the Case were we are in a drive Task
                final AVTask avTask = (AVTask) currentTask;
                if (avTask.getAVTaskType().equals(AVTaskType.DRIVE)) {
                    AVDriveTask avDriveTask = (AVDriveTask) avTask;
                    // AS there is no task after this one and the currend destinadtion is not the current location we have to divert the robo taxi to the current location
                    if (!roboTaxi.getDivertableLocation().equals(avDriveTask.getPath().getToLink())) {
                        SharedCourse redirectCourse = SharedCourse.redirectCourse(roboTaxi.getDivertableLocation(),
                                Double.toString(now) + "_currentLink_" + roboTaxi.getId().toString());
                        roboTaxi.addRedirectCourseToMenuAtBegining(redirectCourse);
                        return Optional.of(roboTaxi.getDivertableLocation());
                    }
                    // TODO remove soon if no errors
                    GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
                } else {
                    // We only do it for Drive Tasks. As:
                    // a) A dropoff Task already finishes by default with a stay task afterwards. Thus The only reason we reach this part is because we are in Dropoff
                    GlobalAssert.that(avTask.getAVTaskType().equals(AVTaskType.DROPOFF));
                    GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                    // b) A stay task should never be the second last Task.
                    // c) A pickup Task always needs to have a next course in the menu. but that we can check as well
                }
            } else {
                System.out.println("Thats a case is not allowed. It means that we plan more than two tasks ahead");
                System.out.println("This is only allowed after the redispatchInternal() until the end of the Time Step");
                GlobalAssert.that(false);
            }
        }
        return Optional.empty();

    }

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
    /* package */ final static void setRoboTaxiDiversion(RoboTaxi sRoboTaxi, Link destination, FuturePathFactory futurePathFactory, double now, EventsManager eventsManager) {
        GlobalAssert.that(RoboTaxiUtils.hasNextCourse(sRoboTaxi));
        // update Status Of Robo Taxi
        // In Handle

        // update schedule ofsRoboTaxi
        final Schedule schedule = sRoboTaxi.getSchedule();
        Task task = schedule.getCurrentTask(); // <- implies that task is started
        new RoboTaxiTaskAdapter(task) {

            @Override
            public void handle(AVDriveTask avDriveTask) {
                if (!avDriveTask.getPath().getToLink().equals(destination)) { // ignore when vehicle is already going
                    FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                            sRoboTaxi.getDivertableLocation(), destination, sRoboTaxi.getDivertableTime());

                    sRoboTaxi.assignDirective(new DriveVehicleDiversionDirective(sRoboTaxi, destination, futurePathContainer));
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
                    SharedCourse nextCourse = RoboTaxiUtils.getStarterCourse(sRoboTaxi).get();
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

                if (thisIsLastTimeStep(dropOffTask, now, SharedUniversalDispatcher.SIMTIMESTEP)) {
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
                boolean isOnLastTask = thisIsLastTimeStep(task, now, SharedUniversalDispatcher.SIMTIMESTEP);
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

        GlobalAssert.that(maxTwoMoreTaskAfterThisOneWhichEnds(schedule, task, now, SharedUniversalDispatcher.SIMTIMESTEP));
    }

    private static boolean nextCourseIsRedirectToCurrentLink(RoboTaxi roboTaxi) {
        Optional<SharedCourse> redirectCourseCheck = RoboTaxiUtils.getStarterCourse(roboTaxi);
        if (!redirectCourseCheck.isPresent())
            return false;
        if (!redirectCourseCheck.get().getMealType().equals(SharedMealType.REDIRECT))
            return false;
        return redirectCourseCheck.get().getLink().equals(roboTaxi.getDivertableLocation());
    }

    private static void removeRedirectionToDivertableLocationInBeginning(RoboTaxi roboTaxi) {
        while (nextCourseIsRedirectToCurrentLink(roboTaxi)) {
            roboTaxi.finishRedirection();
        }
    }

}
