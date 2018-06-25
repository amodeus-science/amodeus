/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.tracker.TaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDriveTaskTracker;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** The purpose of RoboTaxiMaintainer is to register {@link AVVehicle} and provide the collection of
 * available vehicles to derived class.
 * <p>
 * manages assignments of {@link AbstractDirective} to {@link AVVehicle}s. path computations
 * attached to assignments are computed in parallel
 * {@link ParallelLeastCostPathCalculator}. */
/* package */ abstract class RoboTaxiMaintainer implements AVDispatcher {
    protected final EventsManager eventsManager;
    private final List<UnitCapRoboTaxi> roboTaxis = new ArrayList<>();
    private Double private_now = null;
    public InfoLine infoLine = null;
    private final StorageUtils storageUtils;

    RoboTaxiMaintainer(EventsManager eventsManager, Config config, AVDispatcherConfig avDispatcherConfig) {
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        this.eventsManager = eventsManager;
        this.infoLine = new InfoLine(safeConfig.getInteger("infoLinePeriod", 10));
        String outputdirectory = config.controler().getOutputDirectory();
        this.storageUtils = new StorageUtils(new File(outputdirectory));

    }

    /** @return time of current re-dispatching iteration step
     * @throws NullPointerException
     *             if dispatching has not started yet */
    protected final double getTimeNow() {
        return private_now;
    }

    /** @return collection of RoboTaxis */
    protected final List<UnitCapRoboTaxi> getRoboTaxis() {
        if (roboTaxis.isEmpty() || !roboTaxis.get(0).getSchedule().getStatus().equals(Schedule.ScheduleStatus.STARTED))
            return Collections.emptyList();
        return Collections.unmodifiableList(roboTaxis);
    }

    private void updateDivertableLocations() {
        for (UnitCapRoboTaxi robotaxi : getRoboTaxis()) {
            GlobalAssert.that(robotaxi.isWithoutDirective());
            Schedule schedule = robotaxi.getSchedule();
            new RoboTaxiTaskAdapter(schedule.getCurrentTask()) {
                @Override
                public void handle(AVDriveTask avDriveTask) {
                    // for empty cars the drive task is second to last task
                    if (ScheduleUtils.isNextToLastTask(schedule, avDriveTask)) {
                        TaskTracker taskTracker = avDriveTask.getTaskTracker();
                        AmodeusDriveTaskTracker onlineDriveTaskTracker = (AmodeusDriveTaskTracker) taskTracker;
                        LinkTimePair linkTimePair = onlineDriveTaskTracker.getSafeDiversionPoint();
                        robotaxi.setDivertableLinkTime(linkTimePair); // contains null check
                        robotaxi.setCurrentDriveDestination(avDriveTask.getPath().getToLink());
                        GlobalAssert.that(!robotaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                    } else
                        GlobalAssert.that(robotaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                }

                @Override
                public void handle(AVPickupTask avPickupTask) {
                    GlobalAssert.that(robotaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                }

                @Override
                public void handle(AVDropoffTask avDropOffTask) {
                    GlobalAssert.that(robotaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                }

                @Override
                public void handle(AVStayTask avStayTask) {
                    // for empty vehicles the current task has to be the last task
                    if (ScheduleUtils.isLastTask(schedule, avStayTask) && !isInPickupRegister(robotaxi)) {
                        GlobalAssert.that(avStayTask.getBeginTime() <= getTimeNow());
                        GlobalAssert.that(avStayTask.getLink() != null);
                        robotaxi.setDivertableLinkTime(new LinkTimePair(avStayTask.getLink(), getTimeNow()));
                        robotaxi.setCurrentDriveDestination(avStayTask.getLink());
                        robotaxi.setStatus(RoboTaxiStatus.STAY);
                    }
                }
            };
        }
    }

    @Override
    public final void addVehicle(AVVehicle vehicle) {
        roboTaxis.add(new UnitCapRoboTaxi(vehicle, new LinkTimePair(vehicle.getStartLink(), 0.0), vehicle.getStartLink()));
        eventsManager.processEvent(new AVVehicleAssignmentEvent(vehicle, 0));
    }

    @Override
    public final void onNextTimestep(double now) {
        private_now = now; // <- time available to derived class via getTimeNow()
        updateInfoLine();
        notifySimulationSubscribers(Math.round(now), storageUtils);
        consistencyCheck();
        beforeStepTasks(); // <- if problems with RoboTaxi Status to Completed consider to set "simEndtimeInterpretation" to "null"
        executePickups();
        executeDropoffs();
        redispatch(now);
        afterStepTasks();
        executeDirectives();
        consistencyCheck();
    }

    protected void updateInfoLine() {
        String infoLine = getInfoLine();
        this.infoLine.updateInfoLine(infoLine, getTimeNow());
    }

    /** derived classes should override this function to add details
     * 
     * @return String with infoLine content */
    protected String getInfoLine() {
        final String string = getClass().getSimpleName() + "        ";
        return String.format("%s@%6d V=(%4ds,%4dd)", //
                string.substring(0, 6), //
                (long) getTimeNow(), //
                roboTaxis.stream().filter(rt -> rt.isInStayTask()).count(), //
                roboTaxis.stream().filter(rt -> rt.getStatus().isDriving()).count());
    }

    private void beforeStepTasks() {

        // update divertable locations of RoboTaxis
        updateDivertableLocations();

        // update current locations of RoboTaxis
        if (private_now > 0) { // at time 0, tasks are not started.
            updateCurrentLocations();
        }

    }

    private void afterStepTasks() {
        stopAbortedPickupRoboTaxis();
    }

    private void consistencyCheck() {
        consistencySubCheck();

    }

    private void executeDirectives() {
        roboTaxis.stream().filter(rt -> !rt.isWithoutDirective()).forEach(UnitCapRoboTaxi::executeDirective);
    }

    private void updateCurrentLocations() {
        @SuppressWarnings("unused")
        int failed = 0;
        if (!roboTaxis.isEmpty()) {
            for (UnitCapRoboTaxi robotaxi : roboTaxis) {
                final Link link = RoboTaxiLocation.of(robotaxi);
                if (link != null) {
                    robotaxi.setLastKnownLocation(link);
                } else {
                    ++failed;
                }

            }
        }
    }

    /* package */ abstract void executePickups();

    /* package */ abstract void executeDropoffs();

    /* package */ abstract void stopAbortedPickupRoboTaxis();

    /* package */ abstract void consistencySubCheck();

    /* package */ abstract void notifySimulationSubscribers(long round_now, StorageUtils storageUtils);

    /* package */ abstract boolean isInPickupRegister(UnitCapRoboTaxi robotaxi);

    @Override
    public final void onNextTaskStarted(AVVehicle task) {
        // intentionally empty
    }

    /** derived classes should override this function
     * 
     * @param now */
    protected abstract void redispatch(double now);

}
