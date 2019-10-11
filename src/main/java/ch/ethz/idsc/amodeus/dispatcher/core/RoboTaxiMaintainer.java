/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** The purpose of RoboTaxiMaintainer is to register {@link AVVehicle} and provide the collection of
 * available vehicles to derived class.
 * <p>
 * manages assignments of {@link DirectiveInterface} to {@link AVVehicle}s. path computations
 * attached to assignments are computed in parallel
 * {@link ParallelLeastCostPathCalculator}. */
/* package */ abstract class RoboTaxiMaintainer implements AVDispatcher {
    protected final EventsManager eventsManager;
    private final List<RoboTaxi> roboTaxis = new ArrayList<>();
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

    /** @return {@link List} of {@link RoboTaxi} */
    protected final List<RoboTaxi> getRoboTaxis() {
        if (roboTaxis.isEmpty() || !roboTaxis.get(0).getSchedule().getStatus().equals(Schedule.ScheduleStatus.STARTED))
            return Collections.emptyList();
        return Collections.unmodifiableList(roboTaxis);
    }

    protected abstract void updateDivertableLocations();

    public final void addRoboTaxi(RoboTaxi roboTaxi, Event event) {
        roboTaxis.add(roboTaxi);
        eventsManager.processEvent(event);
    }

    @Override
    public abstract void addVehicle(AVVehicle vehicle);

    /** functions called at every MATSim timestep, dispatching action happens in <b> redispatch <b> */
    @Override
    public final void onNextTimestep(double now) {

        private_now = now; // <- time available to derived class via getTimeNow()
        updateInfoLine();
        notifySimulationSubscribers(Math.round(now), storageUtils);
        consistencyCheck();
        beforeStepTasks(); // <- if problems with RoboTaxi Status to Completed consider to set "simEndtimeInterpretation" to "null"
        // The Dropoff is before the pickup because:
        // a) A robotaxi which picks up a customer should not dropoff one at the same time step
        // b) but in the shared case the internal dropoff should be able to finish a dropoff which enables the pickups to be executed
        executeDropoffs();
        executePickups();
        executeRedirects();
        redispatch(now);
        redispatchInternal(now);
        afterStepTasks();
        executeDirectives();
        consistencyCheck();

    }

    /** the info line is displayed in the console at every dispatching timestep and in the
     * AMoDeus viewer */
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
        updateDivertableLocations();
        if (private_now > 0) { // at time 0, tasks are not started.
            Long time = (long) ((double) private_now);
            updateCurrentInformation(time);
        }
    }

    /** {@link RoboTaxi} on a pickup ride which are sent to another location are
     * stopped, also taxis which have lost their pickup assignment */
    private void afterStepTasks() {
        stopAbortedPickupRoboTaxis();
        // flushLocationTraces();

    }

    private void consistencyCheck() {
        consistencySubCheck();
    }

    private void executeDirectives() {
        roboTaxis.stream().filter(rt -> !rt.isWithoutDirective()).forEach(RoboTaxi::executeDirective);
    }

    private void updateCurrentInformation(Long time) {
        @SuppressWarnings("unused")
        int failed = 0;
        if (!roboTaxis.isEmpty()) {
            for (RoboTaxi robotaxi : roboTaxis) {
                final Link link = RoboTaxiLocation.of(robotaxi);
<<<<<<< HEAD
                if (link != null) {
                    robotaxi.lastKnownTime = time;
                    robotaxi.addKnownLocation(time, link);
=======
                if (Objects.nonNull(link)) {
                    robotaxi.setLastKnownLocation(link);
                    updateLocationTrace(robotaxi, link);
>>>>>>> master
                } else {
                    ++failed;
                }
            }
        }
    }

    /* package */ abstract void updateLocationTrace(RoboTaxi roboTaxi, Link lastKnownLoc);

    /* package */ abstract void executePickups();

    /* package */ abstract void executeDropoffs();

    /* package */ abstract void stopAbortedPickupRoboTaxis();

    /* package */ abstract void consistencySubCheck();

    /* package */ abstract void notifySimulationSubscribers(long round_now, StorageUtils storageUtils);

    /* package */ abstract void redispatchInternal(double now);

    /* package */ abstract void executeRedirects();

    @Override
    public final void onNextTaskStarted(AVVehicle task) {
        // intentionally empty
    }

    /** derived classes should override this function
     * 
     * @param now */
    protected abstract void redispatch(double now);

}