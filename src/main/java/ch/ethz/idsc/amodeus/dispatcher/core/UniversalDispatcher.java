/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.SimulationDistribution;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.amodeus.net.SimulationObjects;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** purpose of {@link UniversalDispatcher} is to collect and manage {@link AVRequest}s alternative
 * implementation of {@link AVDispatcher}; supersedes
 * {@link AbstractDispatcher}. */
/* added new requestRegister which does hold information from request to dropoff of each request
 * which is written to the simulationObject
 * implementation is not very clean yet but functionally stable, depending on the publishPeriod, andya jan '18 */
public abstract class UniversalDispatcher extends RoboTaxiMaintainer {
    private final FuturePathFactory futurePathFactory;
    private final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
    private final Map<AVRequest, RoboTaxi> pickupRegister = new HashMap<>(); // new RequestRegister
    private final Map<AVRequest, RoboTaxi> requestRegister = new HashMap<>();
    private final Map<AVRequest, RoboTaxi> periodFulfilledRequests = new HashMap<>(); // new temporaryRequestRegister for fulfilled requests
    private final Map<Id<Vehicle>, RoboTaxiStatus> oldRoboTaxis = new HashMap<>();
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;
    protected int publishPeriod; // not final, so that dispatchers can disable, or manipulate
    private int total_matchedRequests = 0;

    protected UniversalDispatcher( //
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            EventsManager eventsManager //
    ) {
        super(eventsManager, config, avDispatcherConfig);
        futurePathFactory = new FuturePathFactory(parallelLeastCostPathCalculator, travelTime);
        pickupDurationPerStop = avDispatcherConfig.getParent().getTimingParameters().getPickupDurationPerStop();
        dropoffDurationPerStop = avDispatcherConfig.getParent().getTimingParameters().getDropoffDurationPerStop();
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        publishPeriod = safeConfig.getInteger("publishPeriod", 10);
    }

    // ===================================================================================
    // Methods to use EXTERNALLY in derived dispatchers

    /** @return {@Collection} of all {@AVRequests} which are currently open. Requests are removed from list in setAcceptRequest function */
    // TODO document why function became public
    public synchronized final Collection<AVRequest> getAVRequests() {
        return Collections.unmodifiableCollection(pendingRequests);
    }

    /** @return AVRequests which are currently not assigned to a vehicle */
    protected synchronized final List<AVRequest> getUnassignedAVRequests() {
        return pendingRequests.stream() //
                .filter(r -> !pickupRegister.containsKey(r)) //
                .collect(Collectors.toList());
    }

    /** Example call: getRoboTaxiSubset(AVStatus.STAY, AVStatus.DRIVEWITHCUSTOMER)
     * 
     * @param status {@AVStatus} of desired robotaxis, e.g., STAY,DRIVETOCUSTOMER,...
     * @return list of robotaxis which are in {@AVStatus} status */
    public final List<RoboTaxi> getRoboTaxiSubset(RoboTaxiStatus... status) {
        return getRoboTaxiSubset(EnumSet.copyOf(Arrays.asList(status)));
    }

    private List<RoboTaxi> getRoboTaxiSubset(Set<RoboTaxiStatus> status) {
        return getRoboTaxis().stream().filter(rt -> status.contains(rt.getStatus())).collect(Collectors.toList());
    }

    /** @return divertable robotaxis which currently not on a pickup drive */
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxis() {
        Collection<RoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> !pickupRegister.containsValue(rt)) //
                .collect(Collectors.toList());
        GlobalAssert.that(!divertableUnassignedRoboTaxis.stream().anyMatch(pickupRegister::containsValue));
        GlobalAssert.that(divertableUnassignedRoboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
        return divertableUnassignedRoboTaxis;
    }

    /** @return {@Collection} of {@RoboTaxi} which can be redirected during iteration */
    protected final Collection<RoboTaxi> getDivertableRoboTaxis() {
        return getRoboTaxis().stream() //
                .filter(RoboTaxi::isWithoutDirective) //
                .filter(RoboTaxi::isWithoutCustomer) //
                .filter(RoboTaxi::notDrivingOnLastLink) //
                .collect(Collectors.toList());
    }

    /** @return immutable and inverted copy of pickupRegister, displays which vehicles are currently scheduled to pickup which request */
    protected final Map<RoboTaxi, AVRequest> getPickupRoboTaxis() {
        Map<RoboTaxi, AVRequest> pickupPairs = pickupRegister.entrySet().stream()//
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        GlobalAssert.that(pickupPairs.keySet().stream().allMatch(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER)));
        return pickupPairs;
    }

    /** Diverts {@roboTaxi} to Link if {@avRequest} and adds pair to pickupRegister. If the {@roboTaxi} was scheduled to pickup another {@AVRequest}, then this
     * pair is silently revmoved from the pickup register which is a bijection of {@RoboTaxi} and open {@AVRequest}.
     * 
     * @param roboTaxi
     * @param avRequest */
    public void setRoboTaxiPickup(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        GlobalAssert.that(pendingRequests.contains(avRequest));

        // 1) enter information into pickup table
        // 2) also do everything for the full-time requestRegister
        if (!pickupRegister.containsValue(roboTaxi)) { // roboTaxi was not picking up
            pickupRegister.put(avRequest, roboTaxi);
            requestRegister.put(avRequest, roboTaxi);
        } else {
            AVRequest toRemove = pickupRegister.entrySet().stream()//
                    .filter(e -> e.getValue().equals(roboTaxi)).findAny().get().getKey();
            pickupRegister.remove(toRemove); // remove AVRequest/RoboTaxi pair served before by roboTaxi
            pickupRegister.remove(avRequest); // remove AVRequest/RoboTaxi pair corresponding to avRequest
            requestRegister.remove(toRemove);
            requestRegister.remove(avRequest);
            pickupRegister.put(avRequest, roboTaxi); // add new pair
            requestRegister.put(avRequest, roboTaxi);
        }
        GlobalAssert.that(pickupRegister.size() == pickupRegister.values().stream().distinct().count());

        // 2) set vehicle diversion
        setRoboTaxiDiversion(roboTaxi, avRequest.getFromLink(), RoboTaxiStatus.DRIVETOCUSTOMER);
    }

    // ===================================================================================
    // INTERNAL Methods, do not call from derived dispatchers.

    /** For UniversalDispatcher, VehicleMaintainer internal use only. Use {@link UniveralDispatcher.setRoboTaxiPickup} or
     * {@link setRoboTaxiRebalance} from dispatchers. Assigns new destination to vehicle, if vehicle is already located at destination, nothing
     * happens. In one pass of {@redispatch(...)} in {@VehicleMaintainer}, the function setVehicleDiversion(...) may only be invoked
     * once for a single {@RoboTaxi} vehicle
     *
     * @param robotaxi {@link RoboTaxi} supplied with a getFunction,e.g., {@link this.getDivertableRoboTaxis}
     * @param destination {@link Link} the {@link RoboTaxi} should be diverted to
     * @param avstatus {@link} the {@link AVStatus} the {@link RoboTaxi} has after the diversion, depends if used from {@link setRoboTaxiPickup} or
     *            {@link setRoboTaxiRebalance} */
    final void setRoboTaxiDiversion(RoboTaxi robotaxi, Link destination, RoboTaxiStatus avstatus) {
        // updated status of robotaxi
        GlobalAssert.that(robotaxi.isWithoutCustomer());
        GlobalAssert.that(robotaxi.isWithoutDirective());

        robotaxi.setStatus(avstatus);

        // udpate schedule of robotaxi
        final Schedule schedule = robotaxi.getSchedule();
        Task task = schedule.getCurrentTask(); // <- implies that task is started
        new RoboTaxiTaskAdapter(task) {
            @Override
            public void handle(AVDriveTask avDriveTask) {
                if (!avDriveTask.getPath().getToLink().equals(destination)) { // ignore when vehicle is already going there
                    FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                            robotaxi.getDivertableLocation(), destination, robotaxi.getDivertableTime());
                    robotaxi.assignDirective(new DriveVehicleDiversionDirective(robotaxi, destination, futurePathContainer));
                } else
                    robotaxi.assignDirective(EmptyDirective.INSTANCE);
            }

            @Override
            public void handle(AVStayTask avStayTask) {
                if (!avStayTask.getLink().equals(destination)) { // ignore request where location == target
                    FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                            robotaxi.getDivertableLocation(), destination, robotaxi.getDivertableTime());
                    robotaxi.assignDirective(new StayVehicleDiversionDirective(robotaxi, destination, futurePathContainer));
                } else
                    robotaxi.assignDirective(EmptyDirective.INSTANCE);
            }
        };
    }

    /** Function called from {@link UniversalDispatcher.executePickups} if a RoboTaxi scheduled for pickup has reached the
     * from link of the {@link AVRequest}.
     * 
     * @param roboTaxi
     * @param avRequest */
    private synchronized final void setAcceptRequest(RoboTaxi roboTaxi, AVRequest avRequest) {
        roboTaxi.setStatus(RoboTaxiStatus.DRIVEWITHCUSTOMER);
        roboTaxi.setCurrentDriveDestination(avRequest.getFromLink());
        {
            boolean statusPen = pendingRequests.remove(avRequest);
            GlobalAssert.that(statusPen);
        }
        {
            RoboTaxi former = pickupRegister.remove(avRequest);
            GlobalAssert.that(roboTaxi == former);
        }

        consistencySubCheck();

        final Schedule schedule = roboTaxi.getSchedule();
        // check that current task is last task in schedule
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule));

        final double endPickupTime = getTimeNow() + pickupDurationPerStop;
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getFromLink(), avRequest.getToLink(), endPickupTime);

        roboTaxi.assignDirective(new AcceptRequestDirective(roboTaxi, avRequest, futurePathContainer, getTimeNow(), dropoffDurationPerStop));

        ++total_matchedRequests;
    }

    /** Function called from {@link UniversalDispatcher.executeDropoffs} if a RoboTaxi scheduled for dropoff has reached the
     * from link of the {@link AVRequest}.
     * 
     * @param roboTaxi
     * @param avRequest */
    private synchronized final void setPassengerDropoff(RoboTaxi roboTaxi, AVRequest avRequest) {
        RoboTaxi former = requestRegister.remove(avRequest);
        GlobalAssert.that(roboTaxi == former);

        // save avRequests which are matched for one publishPeriod to ensure no requests are lost in the recording.
        periodFulfilledRequests.put(avRequest, roboTaxi);

        final Schedule schedule = roboTaxi.getSchedule();
        // check that current task is last task in schedule
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule));
    }

    @Override
    /* package */ final boolean isInPickupRegister(RoboTaxi robotaxi) {
        return pickupRegister.containsValue(robotaxi);
    }

    /** @param avRequest
     * @return robotaxi assigned to given avRequest, or null if no taxi is assigned to avRequest */
    public final Optional<RoboTaxi> getPickupTaxi(AVRequest avRequest) {
        return Optional.ofNullable(pickupRegister.get(avRequest));
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the fromLink of an {@link AVRequest} */
    @Override
    void executePickups() {
        Map<AVRequest, RoboTaxi> pickupRegisterCopy = new HashMap<>(pickupRegister);
        for (Entry<AVRequest, RoboTaxi> entry : pickupRegisterCopy.entrySet()) {
            AVRequest avRequest = entry.getKey();
            GlobalAssert.that(pendingRequests.contains(avRequest));
            RoboTaxi pickupVehicle = entry.getValue();
            Link pickupVehicleLink = pickupVehicle.getDivertableLocation();
            boolean isOk = pickupVehicle.getSchedule().getCurrentTask() == Schedules.getLastTask(pickupVehicle.getSchedule());
            if (avRequest.getFromLink().equals(pickupVehicleLink) && isOk) {
                setAcceptRequest(pickupVehicle, avRequest);
            }
        }
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the toLink of an {@link AVRequest} */
    @Override
    void executeDropoffs() {
        Map<AVRequest, RoboTaxi> requestRegisterCopy = new HashMap<>(requestRegister);
        for (Entry<AVRequest, RoboTaxi> entry : requestRegisterCopy.entrySet()) {
            if (Objects.nonNull(entry.getValue())) {
                AVRequest avRequest = entry.getKey();
                RoboTaxi dropoffVehicle = entry.getValue();
                Link dropoffVehicleLink = dropoffVehicle.getDivertableLocation();
                boolean isOk = dropoffVehicle.getSchedule().getCurrentTask() == Schedules.getLastTask(dropoffVehicle.getSchedule());
                if (avRequest.getToLink().equals(dropoffVehicleLink) && isOk) {
                    setPassengerDropoff(dropoffVehicle, avRequest);
                }
            }
        }
    }

    /** called when a new request enters the system, adds request to {@link pendingRequests}, needs to be public because called from
     * other not derived MATSim functions which are located in another package */
    @Override
    public final void onRequestSubmitted(AVRequest request) {
        boolean added = pendingRequests.add(request); // <- store request
        requestRegister.put(request, null);
        GlobalAssert.that(added);
    }

    /** function stops {@link RoboTaxi} which are still heading towards an {@link AVRequest} but another {@link RoboTaxi} was scheduled to pickup this
     * {@link AVRequest} in the meantime */
    @Override
    /* package */ final void stopAbortedPickupRoboTaxis() {

        // stop vehicles still driving to a request but other taxi serving that request already
        getRoboTaxis().stream()//
                .filter(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER))//
                .filter(rt -> !pickupRegister.containsValue(rt))//
                .filter(RoboTaxi::isWithoutCustomer)//
                .filter(RoboTaxi::isWithoutDirective)//
                .forEach(rt -> setRoboTaxiDiversion(rt, rt.getDivertableLocation(), RoboTaxiStatus.REBALANCEDRIVE));
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());
    }

    /** Consistency checks to be called by {@link RoboTaxiMaintainer.consistencyCheck} in each iteration. */
    @Override
    protected final void consistencySubCheck() {
        // there cannot be more pickup vehicles than open requests
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());

        // pickupRegister needs to be a subset of requestRegister
        pickupRegister.keySet().forEach(a -> GlobalAssert.that(requestRegister.containsKey(a)));

        // containment check pickupRegister and pendingRequests
        pickupRegister.keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

        // ensure no robotaxi is scheduled to pickup two requests
        GlobalAssert.that(pickupRegister.size() == pickupRegister.values().stream().distinct().count());

    }

    /** save simulation data into {@link SimulationObject} for later analysis and visualization. */
    @Override
    protected final void notifySimulationSubscribers(long round_now, StorageUtils storageUtils) {
        if (publishPeriod > 0 && round_now % publishPeriod == 0) {
            SimulationObjectCompiler simulationObjectCompiler = SimulationObjectCompiler.create( //
                    round_now, getInfoLine(), total_matchedRequests);

            Map<AVRequest, RoboTaxi> newRegister = requestRegister;
            List<RoboTaxi> newRoboTaxis = getRoboTaxis();

            simulationObjectCompiler.insertFulfilledRequests(periodFulfilledRequests);
            simulationObjectCompiler.insertRequests(newRegister, oldRoboTaxis);

            simulationObjectCompiler.insertVehicles(newRoboTaxis);
            SimulationObject simulationObject = simulationObjectCompiler.compile();

            // in the first pass, the vehicles is typically empty
            // in that case, the simObj will not be stored or communicated
            if (SimulationObjects.hasVehicles(simulationObject)) {
                // store simObj and distribute to clients
                SimulationDistribution.of(simulationObject, storageUtils);
            }

            oldRoboTaxis.clear();
            newRoboTaxis.forEach(r -> oldRoboTaxis.put(r.getId(), r.getStatus()));

            periodFulfilledRequests.clear();
        }
    }

    /** adds information to InfoLine */
    @Override
    protected String getInfoLine() {
        return String.format("%s R=(%5d) MR=%6d", //
                super.getInfoLine(), //
                getAVRequests().size(), //
                total_matchedRequests);
    }

}
