/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.TaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDriveTaskTracker;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationDistribution;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.amodeus.net.SimulationObjects;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** purpose of {@link UniversalDispatcher} is to collect and manage {@link AVRequest}s alternative
 * implementation of {@link AVDispatcher}; supersedes
 * {@link AbstractDispatcher}. */
public abstract class UniversalDispatcher extends RoboTaxiMaintainer {
    private final MatsimAmodeusDatabase db;
    private final FuturePathFactory futurePathFactory;
    private final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
    private final Map<AVRequest, RoboTaxi> pickupRegister = new HashMap<>();
    private final Map<AVRequest, RoboTaxi> rqstDrvRegister = new HashMap<>();
    private final Map<AVRequest, RoboTaxi> periodFulfilledRequests = new HashMap<>();
    private final Set<AVRequest> periodAssignedRequests = new HashSet<>();
    private final Set<AVRequest> periodPickedUpRequests = new HashSet<>();
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;
    protected int publishPeriod; // not final, so that dispatchers can disable, or manipulate
    private int total_matchedRequests = 0;

    protected UniversalDispatcher( //
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(eventsManager, config, avDispatcherConfig);
        this.db = db;
        futurePathFactory = new FuturePathFactory(parallelLeastCostPathCalculator, travelTime);
        pickupDurationPerStop = avDispatcherConfig.getParent().getTimingParameters().getPickupDurationPerStop();
        dropoffDurationPerStop = avDispatcherConfig.getParent().getTimingParameters().getDropoffDurationPerStop();
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        publishPeriod = safeConfig.getInteger("publishPeriod", 10);
    }

    // ===================================================================================
    // Methods to use EXTERNALLY in derived dispatchers

    /** @return {@Collection} of all {@AVRequests} which are currently open. Requests are removed from list in setAcceptRequest function. */
    protected synchronized final Collection<AVRequest> getAVRequests() {
        return Collections.unmodifiableCollection(pendingRequests);
    }

    /** @return {@link AVRequests}s currently not assigned to a vehicle */
    protected synchronized final List<AVRequest> getUnassignedAVRequests() {
        return pendingRequests.stream() //
                .filter(r -> !pickupRegister.containsKey(r)) //
                .collect(Collectors.toList());
    }

    /** Example call: getRoboTaxiSubset(RoboTaxiStatus.STAY, RoboTaxiStatus.DRIVEWITHCUSTOMER)
     * 
     * @param status {@ARoboTaxiStatus} of desired robotaxis, e.g., STAY,DRIVETOCUSTOMER,...
     * @return list of {@link RoboTaxi}s which are in {@AVStatus} status */
    public final List<RoboTaxi> getRoboTaxiSubset(RoboTaxiStatus... status) {
        return getRoboTaxiSubset(EnumSet.copyOf(Arrays.asList(status)));
    }

    private List<RoboTaxi> getRoboTaxiSubset(Set<RoboTaxiStatus> status) {
        return getRoboTaxis().stream().filter(rt -> status.contains(rt.getStatus())).collect(Collectors.toList());
    }

    /** @return divertable {@link RoboTaxi}s which currently not on a pickup drive */
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxis() {
        Collection<RoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> !pickupRegister.containsValue(rt)) //
                .collect(Collectors.toList());
        GlobalAssert.that(!divertableUnassignedRoboTaxis.stream().anyMatch(pickupRegister::containsValue));
        GlobalAssert.that(divertableUnassignedRoboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
        return divertableUnassignedRoboTaxis;
    }

    /** @return {@Collection} of {@RoboTaxi}s which can be redirected during iteration */
    protected final Collection<RoboTaxi> getDivertableRoboTaxis() {
        return getRoboTaxis().stream() //
                .filter(RoboTaxi::isDivertable)//
                .collect(Collectors.toList());
    }

    /** @return immutable and inverted copy of pickupRegister, displays which vehicles are currently scheduled to pickup which request */
    protected final Map<RoboTaxi, AVRequest> getPickupRoboTaxis() {
        Map<RoboTaxi, AVRequest> pickupPairs = pickupRegister.entrySet().stream()//
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        GlobalAssert.that(pickupPairs.keySet().stream().allMatch(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER)));
        return pickupPairs;
    }

    /** Diverts {@link RoboTaxi} to {@link Link} of {@link AVRequest} and adds pair to pickupRegister.
     * If the {@link RoboTaxi} was scheduled to pickup another {@link AVRequest}, then this
     * pair is silently revmoved from the pickup register.
     * 
     * @param roboTaxi
     * @param avRequest */
    public void setRoboTaxiPickup(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        GlobalAssert.that(pendingRequests.contains(avRequest));

        /** for some dispatchers, reassignment is permanently invoked again,
         * the {@link RoboTaxi} should appear under only at the time step of assignment */
        if (!pickupRegister.containsKey(avRequest))
            periodAssignedRequests.add(avRequest);

        // 1) enter information into pickup table
        if (!pickupRegister.containsValue(roboTaxi))
            pickupRegister.put(avRequest, roboTaxi);
        else {
            AVRequest toRemove = pickupRegister.entrySet().stream()//
                    .filter(e -> e.getValue().equals(roboTaxi)).findAny().get().getKey();
            pickupRegister.remove(toRemove); // remove AVRequest/RoboTaxi pair served before by roboTaxi
            pickupRegister.remove(avRequest); // remove AVRequest/RoboTaxi pair corresponding to avRequest
            pickupRegister.put(avRequest, roboTaxi); // add new pair
        }
        GlobalAssert.that(pickupRegister.size() == pickupRegister.values().stream().distinct().count());

        // 2) set vehicle diversion
        setRoboTaxiDiversion(roboTaxi, avRequest.getFromLink(), RoboTaxiStatus.DRIVETOCUSTOMER);
    }

    // ===================================================================================
    // INTERNAL Methods, do not call from derived dispatchers.

    /** For {@link UniversalDispatcher}, {@link VehicleMaintainer} internal use only. Use {@link UniveralDispatcher.setRoboTaxiPickup} or
     * {@link setRoboTaxiRebalance} from dispatchers. Assigns new destination to vehicle, if vehicle is already located at destination, nothing
     * happens. In one pass of {@redispatch(...)} in {@VehicleMaintainer}, the function setVehicleDiversion(...) may only be invoked
     * once for a single {@link RoboTaxi} vehicle
     *
     * @param robotaxi {@link RoboTaxi} supplied with a getFunction,e.g., {@link this.getDivertableRoboTaxis}
     * @param destination {@link Link} the {@link RoboTaxi} should be diverted to
     * @param status {@link} the {@link RoboTaxiStatus} the {@link RoboTaxi} has after the diversion, depends if used from {@link setRoboTaxiPickup} or
     *            {@link setRoboTaxiRebalance} */
    final void setRoboTaxiDiversion(RoboTaxi robotaxi, Link destination, RoboTaxiStatus status) {
        /** update {@link RoboTaxiStatus} of {@link RoboTaxi} */
        GlobalAssert.that(robotaxi.isWithoutCustomer());
        GlobalAssert.that(robotaxi.isWithoutDirective());
        robotaxi.setStatus(status);

        /** update {@link Schedule} of {@link RoboTaxi} */
        final Schedule schedule = robotaxi.getSchedule();
        Task task = schedule.getCurrentTask();
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

    /** Function called from {@link UniversalDispatcher.executePickups} if a {@link RoboTaxi} scheduled for pickup has reached the
     * {@link AVRequest.pickupLink} of the {@link AVRequest}.
     * 
     * @param roboTaxi
     * @param avRequest */
    private synchronized final void setAcceptRequest(RoboTaxi roboTaxi, AVRequest avRequest) {
        roboTaxi.setStatus(RoboTaxiStatus.DRIVEWITHCUSTOMER);
        roboTaxi.setCurrentDriveDestination(avRequest.getFromLink());

        /** request not pending anymore */
        boolean statusPen = pendingRequests.remove(avRequest);
        GlobalAssert.that(statusPen);

        /** request not during pickup anymore */
        RoboTaxi formerpckp = pickupRegister.remove(avRequest);
        GlobalAssert.that(roboTaxi == formerpckp);

        /** now during drive */
        RoboTaxi formerrqstDrv = rqstDrvRegister.put(avRequest, roboTaxi);
        GlobalAssert.that(Objects.isNull(formerrqstDrv));

        /** ensure recorded in {@link SimulationObject} */
        periodPickedUpRequests.add(avRequest);
        consistencySubCheck();

        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule));

        final double endPickupTime = getTimeNow() + pickupDurationPerStop;
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getFromLink(), avRequest.getToLink(), endPickupTime);

        roboTaxi.assignDirective(new AcceptRequestDirective(roboTaxi, avRequest, futurePathContainer, getTimeNow(), dropoffDurationPerStop));

        ++total_matchedRequests;
    }

    /** Function called from {@link UniversalDispatcher.executeDropoffs} if a {@link RoboTaxi} scheduled
     * for dropoff has reached the {@link AVRequest.dropoffLink} of the {@link AVRequest}.
     * 
     * @param roboTaxi
     * @param avRequest */
    private synchronized final void setPassengerDropoff(RoboTaxi roboTaxi, AVRequest avRequest) {
        RoboTaxi former = rqstDrvRegister.remove(avRequest);
        GlobalAssert.that(roboTaxi == former);

        /** save avRequests which are matched for one publishPeriod to ensure requests appear in {@link SimulationObject}s */
        periodFulfilledRequests.put(avRequest, roboTaxi);

        /** check that current task is last task in schedule */
        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule));
    }

    protected final boolean isInPickupRegister(RoboTaxi robotaxi) {
        return pickupRegister.containsValue(robotaxi);
    }

    /* package */ final boolean removeFromPickupRegisters(AVRequest avRequest) {
        RoboTaxi rt1 = pickupRegister.remove(avRequest);
        return Objects.isNull(rt1);
    }

    /** @param avRequest
     * @return {@link RoboTaxi} assigned to given avRequest, or empty if no taxi is assigned to avRequest
     *         Used by BipartiteMatching in euclideanNonCyclic, there a comparison to the old av assignment is needed */
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
        Map<AVRequest, RoboTaxi> requestRegisterCopy = new HashMap<>(rqstDrvRegister);
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
        boolean added = pendingRequests.add(request);
        GlobalAssert.that(added);
    }

    /** function stops {@link RoboTaxi} which are still heading towards an {@link AVRequest} but another {@link RoboTaxi} was scheduled to pickup this
     * {@link AVRequest} in the meantime */
    @Override
    /* package */ final void stopAbortedPickupRoboTaxis() {

        /** stop vehicles still driving to a request but other taxi serving that request already */
        getRoboTaxis().stream()//
                .filter(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER))//
                .filter(rt -> !pickupRegister.containsValue(rt))//
                .filter(RoboTaxi::isWithoutCustomer)//
                .filter(RoboTaxi::isWithoutDirective)//
                .forEach(rt -> setRoboTaxiDiversion(rt, rt.getDivertableLocation(), RoboTaxiStatus.REBALANCEDRIVE));
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());
    }

    /** Consistency checks to be called by {@link RoboTaxiHandler.consistencyCheck} in each iteration. */
    @Override
    protected final void consistencySubCheck() {
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());

        /** containment check pickupRegister and pendingRequests */
        pickupRegister.keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

        /** ensure no robotaxi is scheduled to pickup two requests */
        GlobalAssert.that(pickupRegister.size() == pickupRegister.values().stream().distinct().count());

    }

    /** save simulation data into {@link SimulationObject} for later analysis and visualization. */
    @Override
    protected final void notifySimulationSubscribers(long round_now, StorageUtils storageUtils) {
        if (publishPeriod > 0 && round_now % publishPeriod == 0 && round_now > 1) {
            SimulationObjectCompiler simulationObjectCompiler = SimulationObjectCompiler.create( //
                    round_now, getInfoLine(), total_matchedRequests, db);

            /** pickup register must be after pending requests, request is pending from
             * moment it appears until it is picked up, this period may contain several
             * not connected pickup periods (cancelled pickup attempts) */
            simulationObjectCompiler.insertRequests(pendingRequests, RequestStatus.REQUESTED);
            simulationObjectCompiler.insertRequests(pickupRegister.keySet(), RequestStatus.PICKUPDRIVE);
            simulationObjectCompiler.insertRequests(rqstDrvRegister.keySet(), RequestStatus.DRIVING);

            /** the request is only contained in these three maps durnig 1 time step, which is why
             * they must be inserted after the first three which (potentially) are for multiple
             * time steps. */
            simulationObjectCompiler.insertRequests(periodAssignedRequests, RequestStatus.ASSIGNED);
            simulationObjectCompiler.insertRequests(periodPickedUpRequests, RequestStatus.PICKUP);
            simulationObjectCompiler.insertRequests(periodFulfilledRequests.keySet(), RequestStatus.DROPOFF);

            /** insert {@link RoboTaxi}s */
            simulationObjectCompiler.insertVehicles(getRoboTaxis());

            /** insert information of association of {@link RoboTaxi}s and {@link AVRequest}s */
            simulationObjectCompiler.addRequestRoboTaxiAssoc(pickupRegister);
            simulationObjectCompiler.addRequestRoboTaxiAssoc(rqstDrvRegister);
            simulationObjectCompiler.addRequestRoboTaxiAssoc(periodFulfilledRequests);

            periodFulfilledRequests.clear();
            periodAssignedRequests.clear();
            periodPickedUpRequests.clear();

            /** first pass vehicles typically empty, then no storage / communication of {@link SimulationObject}s */
            SimulationObject simulationObject = simulationObjectCompiler.compile();
            if (SimulationObjects.hasVehicles(simulationObject)) {
                SimulationDistribution.of(simulationObject, storageUtils);
            }
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

    @Override
    final void redispatchInternal(double now) {
        // deliberately empty
    }

    @Override
    final void executeRedirects() {
        // deliberately empty
    }
    
    @Override
    final void executeParking() {
        // deliberately empty
    }

    /** adding a vehicle during setup of simulation, handeled by {@link AVGenerator} */
    @Override
    public final void addVehicle(AVVehicle vehicle) {
        RoboTaxi roboTaxi = new RoboTaxi(vehicle, new LinkTimePair(vehicle.getStartLink(), 0.0), vehicle.getStartLink(), RoboTaxiUsageType.SINGLEUSED);
        Event event = new AVVehicleAssignmentEvent(vehicle, 0);
        addRoboTaxi(roboTaxi, event);
    }

    /** updates the divertable locations, i.e., locations from which a {@link RoboTaxi} can deviate
     * its path according to the current Tasks in the MATSim engine */
    @Override
    protected final void updateDivertableLocations() {
        for (RoboTaxi robotaxi : getRoboTaxis()) {
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

}