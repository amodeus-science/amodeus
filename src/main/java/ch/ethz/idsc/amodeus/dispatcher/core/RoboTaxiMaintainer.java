/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Clips;
import ch.ethz.matsim.av.generator.AVUtils;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import org.matsim.core.utils.io.IOUtils;

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
    private Map<Id<DvrpVehicle>, List<Clip>> serviceSchedules = new HashMap<>();



    RoboTaxiMaintainer(EventsManager eventsManager, Config config, OperatorConfig operatorConfig) {
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        this.eventsManager = eventsManager;
        this.infoLine = new InfoLine(safeConfig.getInteger("infoLinePeriod", 10));
        String outputdirectory = config.controler().getOutputDirectory();
        this.storageUtils = new StorageUtils(new File(outputdirectory));
        config.getModules().get("moia").getParams().get("vehiclesFile");
        BufferedReader br = IOUtils.getBufferedReader(config.getModules().get("moia").getParams().get("vehiclesFile"));

        try {
            br.readLine();
            String info = br.readLine();
            Id<DvrpVehicle> id;

            Clip clip ;
            String[] vehicleInfo = null;
            while(info!= null){
                List<Clip> clipList = new ArrayList<>();
                vehicleInfo = info.split(";");
                clip = Clips.interval(Double.parseDouble(vehicleInfo[3]),Double.parseDouble(vehicleInfo[4]));
                clipList.add(clip);
                id = AVUtils.createId(operatorConfig.getId(),vehicleInfo[0]);
                serviceSchedules.put(id,clipList);
                info = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    protected final void updateInfoLine() {
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
                roboTaxis.stream().filter(RoboTaxi::isInStayTask).count(), //
                roboTaxis.stream().map(RoboTaxi::getStatus).filter(RoboTaxiStatus::isDriving).count());
    }

    private void beforeStepTasks() {
        for (RoboTaxi roboTaxi : roboTaxis) {
            Scalar t = RealScalar.of(getTimeNow());
            Id<DvrpVehicle> roboTaxiId = roboTaxi.getId();

            Boolean clipExists = serviceSchedules.get(roboTaxiId).stream().anyMatch(clip -> clip.isInside(t));
            roboTaxi.setOnService(clipExists);

//            set status of robotaxi to OFFSERVICE, so far causes global assert runtime exception in line 170 of RoboTaxi.java
//            if (!clipExists) {
//                roboTaxi.setStatus(RoboTaxiStatus.OFFSERVICE);
//            }
//            else{
//                roboTaxi.setStatus(RoboTaxiStatus.STAY);
//            }
//            if ( getTimeNow() % 1000 == 0){
//                System.out.println(getTimeNow() +": On service "+ roboTaxi.getOnService() + " status: "+ roboTaxi.getStatus());
//
//            }
        }
// TODO: für alle robotaxis chekcen ob sie on oder off sind

//        davon abhängig setonservice true oder false
        updateDivertableLocations();
        if (private_now > 0) { // at time 0, tasks are not started.
            updateCurrentLocations();
        }

        protectedBeforeStepTasks();
    }

    protected void protectedBeforeStepTasks(){

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

    private void updateCurrentLocations() {
        @SuppressWarnings("unused")
        int failed = 0;
        for (RoboTaxi roboTaxi : roboTaxis) {
            final Link link = RoboTaxiLocation.of(roboTaxi);
            if (Objects.nonNull(link)) {
                roboTaxi.setLastKnownLocation(link);
                updateLocationTrace(roboTaxi, link);
            } else
                ++failed;
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