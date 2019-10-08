/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.ArrayList;
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
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.TaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.shared.Compatibility;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseUtil;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDriveTaskTracker;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationDistribution;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.amodeus.net.SimulationObjects;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.operator.DispatcherConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.TimingConfig;
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

/** purpose of {@link SharedUniversalDispatcher} is to collect and manage
 * {@link AVRequest}s alternative implementation of {@link AVDispatcher};
 * supersedes {@link AbstractDispatcher}. */
public abstract class SharedUniversalDispatcher extends RoboTaxiMaintainer {
    // Registers for Simulation
    private final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
    /** contains all Requests which are not picked Up Yet */
    private final Map<Double, Map<RoboTaxi, AVRequest>> dropOffTimes = new HashMap<>();
    // TODO Shared might be done with robotaxis only?
    private final RequestRegister requestRegister = new RequestRegister();
    /** contains all Requests which are assigned to a RoboTaxi */

    private final Set<RoboTaxi> timeStepReroute = new HashSet<>();

    // Registers for Simulation Objects
    private final Set<AVRequest> periodPickedUpRequests = new HashSet<>();
    private final Map<AVRequest, RoboTaxi> periodFulfilledRequests = new HashMap<>(); // A request is removed from the requestRegister at dropoff. So here we
                                                                                      // store the information
                                                                                      // from which Robotaxi it was droped off
    private final Set<AVRequest> periodAssignedRequests = new HashSet<>();
    private final Set<AVRequest> periodSubmittdRequests = new HashSet<>();
    private final Map<AVRequest, RequestStatus> reqStatuses = new HashMap<>(); // Storing the Request Statuses for the
                                                                               // SimObjects
    // Variables for consistency sub check
    private int total_matchedRequests = 0; // TODO Shared what is the use of this?
    private int total_dropedOffRequests = 0;//

    private final OnboardPassengerCheck onboardPassengerCheck = //
            new OnboardPassengerCheck(total_matchedRequests, total_dropedOffRequests);

    // Simulation Properties
    private final MatsimAmodeusDatabase db;
    private final FuturePathFactory futurePathFactory;
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;
    protected int publishPeriod;
    // not final, so that dispatchers can disable, or manipulate
    /* package */ static final double SIMTIMESTEP = 1.0;// This is used in the Shared Universal Dispatcher to see if a task will end in the next timestep.
    private Double lastTime = null;

    protected SharedUniversalDispatcher( //
            Config config, //
            OperatorConfig operatorConfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(eventsManager, config, operatorConfig);
        this.db = db;
        futurePathFactory = new FuturePathFactory(parallelLeastCostPathCalculator, travelTime);
        pickupDurationPerStop = operatorConfig.getTimingConfig().getPickupDurationPerStop();
        dropoffDurationPerStop = operatorConfig.getTimingConfig().getDropoffDurationPerStop();
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        publishPeriod = safeConfig.getInteger("publishPeriod", 10);
    }

    // ===================================================================================
    // Methods to use EXTERNALLY in derived dispatchers

    /** @return {@Collection} of all {@AVRequests} which are currently open. Requests
     *         are removed from list in setAcceptRequest function. */
    protected synchronized final Collection<AVRequest> getAVRequests() {
        return Collections.unmodifiableCollection(pendingRequests);
    }

    /** @return AVRequests which are currently not assigned to a vehicle */
    protected synchronized final List<AVRequest> getUnassignedAVRequests() {
        return pendingRequests.stream() //
                .filter(r -> !requestRegister.contains(r)) //
                .collect(Collectors.toList());
    }

    /** @return Map of AVRequests which have an assigned Robotaxi but are not Picked up yet. The value is the corresponding RoboTaxi */
    protected final Map<AVRequest, RoboTaxi> getCurrentPickupAssignements() {
        return requestRegister.getPickupRegister(pendingRequests);
    }

    /** @return {@link RoboTaxi} curently scheduled to pickup @param request or null if no {@link RoboTaxi}
     *         is scheduled to pickup the {@link AVRequest} */
    protected final RoboTaxi getCurrentPickupTaxi(AVRequest request) {
        return requestRegister.getPickupRegister(pendingRequests).get(request);
    }

    /** Example call: getRoboTaxiSubset(AVStatus.STAY, AVStatus.DRIVEWITHCUSTOMER)
     * 
     * @param status
     *            {@AVStatus} of desiredsRoboTaxis, e.g., STAY,DRIVETOCUSTOMER,...
     * @return list ofsRoboTaxis which are in {@AVStatus} status */
    protected final List<RoboTaxi> getRoboTaxiSubset(RoboTaxiStatus... status) {
        return getRoboTaxiSubset(EnumSet.copyOf(Arrays.asList(status)));
    }

    private List<RoboTaxi> getRoboTaxiSubset(Set<RoboTaxiStatus> status) {
        return getRoboTaxis().stream().filter(rt -> status.contains(rt.getStatus())).collect(Collectors.toList());
    }

    /** @return divertablesRoboTaxis which currently not on a pickup drive */
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxis() {
        Collection<RoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> !requestRegister.contains(rt)) //
                .collect(Collectors.toList());
        GlobalAssert.that(divertableUnassignedRoboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
        return divertableUnassignedRoboTaxis;
    }

    /** For a SharedRoboTaxi any vehicle can be diverded unless it got a directive in
     * this timestep or it is on the last link
     * 
     * @return {@Collection} of {@RoboTaxi} which can be redirected during iteration */
    protected final Collection<RoboTaxi> getDivertableRoboTaxis() {
        return getRoboTaxis().stream() //
                .filter(RoboTaxi::isDivertable) //
                .collect(Collectors.toList());
    }

    // **********************************************************************************************
    // ********************* EXTERNAL METHODS TO BE USED BY DISPATCHERS *****************************
    // **********************************************************************************************

    /** Function to assign a vehicle to a request. Only to be used in the redispatch function of shared dispatchers.
     * If another vehicle was assigned to this request this assignement will be aborted and replace with the new assignement
     * 
     * @param roboTaxi
     * @param avRequest */
    public void addSharedRoboTaxiPickup(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(pendingRequests.contains(avRequest));

        // If the request was already assigned remove it from the current vehicle in the request register and update its menu;
        if (requestRegister.contains(avRequest)) {
            abortAvRequest(avRequest);
        } else {
            periodAssignedRequests.add(avRequest);
        }

        // update the registers
        requestRegister.add(roboTaxi, avRequest);
        roboTaxi.addAVRequestToMenu(avRequest);
        GlobalAssert.that(SharedCourseUtil.getUniqueAVRequests(roboTaxi.getUnmodifiableViewOfCourses()).contains(avRequest));
        reqStatuses.put(avRequest, RequestStatus.ASSIGNED);
    }

    /** Function to abort an assignment of a request to a roboTaxi.
     * this function can only be called if the request has not been picked up yet and was previously assigned to a robotaxi.
     * Only to be used in the redispatch function of shared dispatchers and internaly in the add shared RoboTaxiPickup.
     * 
     * After the call of this function the request will be in the pending unassigned Requests.
     * After the call of this function the previously assigned Robotaxi will be:
     * a) serving the other customers on board (if there are some)
     * b) rebalancing to the next divertable location (if the menu is empty)
     * 
     * @param avRequest avRequest to abort */
    public final void abortAvRequest(AVRequest avRequest) {
        GlobalAssert.that(requestRegister.contains(avRequest)); // Only already assigned RoboTaxis are considered else you can not call this function
        GlobalAssert.that(pendingRequests.contains(avRequest)); // only if a request is not picked up it makes sense to abort it.
        Optional<RoboTaxi> oldRoboTaxi = requestRegister.getAssignedRoboTaxi(avRequest);
        if (oldRoboTaxi.isPresent()) {
            RoboTaxi roboTaxi = oldRoboTaxi.get();
            requestRegister.remove(roboTaxi, avRequest);
            roboTaxi.removeAVRequestFromMenu(avRequest);
            GlobalAssert.that(Compatibility.of(roboTaxi.getUnmodifiableViewOfCourses()).forCapacity(roboTaxi.getCapacity()));
        } else {
            System.out.println("This place should not be reached");
            GlobalAssert.that(false);
        }
    }

    /** this function will re-route the taxi if it is not in stay task (for
     * congestion relieving purpose) */
    protected void reRoute(RoboTaxi robotaxi) {
        if (!robotaxi.isInStayTask() && robotaxi.canReroute())
            timeStepReroute.add(robotaxi);
    }

    // ***********************************************************************************************
    // ********************* INTERNAL Methods, do not call from derived dispatchers*******************
    // ***********************************************************************************************

    /** carries out the redispatching defined in the {@link SharedMenu} and executes the
     * directives after a check of the menus. */
    @Override
    final void redispatchInternal(double now) {

        /** {@link RoboTaxi} are diverted which:
         * are divertable
         * a) if they have a starter:
         * - do not yet Plan to go to the link of this starter
         * b) if they do not have a starter but are on the way to a location they are stoped */
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            if (timeStepReroute.contains(roboTaxi))
                AdaptMenuToDirective.now(roboTaxi, futurePathFactory, now, eventsManager, true);
            else
                AdaptMenuToDirective.now(roboTaxi, futurePathFactory, now, eventsManager, false);
        }
        timeStepReroute.clear();
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the
     * fromLink of an {@link AVRequest} */
    @Override
    void executePickups() {
        Map<AVRequest, RoboTaxi> pickupRegisterCopy = new HashMap<>(requestRegister.getPickupRegister(pendingRequests));
        List<RoboTaxi> pickupUniqueRoboTaxis = pickupRegisterCopy.values().stream() //
                .filter(srt -> SharedRoboTaxiUtils.isNextCourseOfType(srt, SharedMealType.PICKUP)) //
                .distinct().collect(Collectors.toList());
        for (RoboTaxi roboTaxi : pickupUniqueRoboTaxis) {
            Optional<AVRequest> avRequest = //
                    PickupIfOnLastLink.apply(roboTaxi, getTimeNow(), pickupDurationPerStop, futurePathFactory);
            if (avRequest.isPresent()) {
                GlobalAssert.that(pendingRequests.contains(avRequest.get()));
                // Update the registers
                boolean checkPendingRemoved = pendingRequests.remove(avRequest.get());
                GlobalAssert.that(checkPendingRemoved);
                reqStatuses.put(avRequest.get(), RequestStatus.DRIVING);
                periodPickedUpRequests.add(avRequest.get());
                ++total_matchedRequests;

                GlobalAssert.that(!pendingRequests.contains(avRequest.get()));
                GlobalAssert.that(requestRegister.contains(roboTaxi, avRequest.get()));
            }
        }
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the toLink
     * of an {@link AVRequest} */
    @Override
    void executeDropoffs() {
        /** First the Tasks are assigned. This makes sure the dropoff takes place */
        Map<RoboTaxi, Map<String, AVRequest>> requestRegisterCopy = new HashMap<>(requestRegister.getRegister());
        for (RoboTaxi roboTaxi : requestRegisterCopy.keySet()) {

            Optional<AVRequest> avRequest = AssignSharedDropoffDirective.apply(roboTaxi, getTimeNow(), dropoffDurationPerStop, futurePathFactory);
            if (avRequest.isPresent()) {
                GlobalAssert.that(requestRegister.contains(roboTaxi, avRequest.get()));
                roboTaxi.startDropoff();
                Double endDropOffTime = getTimeNow() + dropoffDurationPerStop;
                if (!dropOffTimes.containsKey(endDropOffTime)) {
                    dropOffTimes.put(endDropOffTime, new HashMap<>());
                }
                dropOffTimes.get(endDropOffTime).put(roboTaxi, avRequest.get());
            }

        }
        /** Until here only the directives were given. The actual drop off takes place now.
         * From the registers the dropoffs are carried out by the dropoffsFormRegisters() function */
        dropoffsFromRegisters();

    }

    private void dropoffsFromRegisters() {
        /** update all dropoffs which finished the task by now */
        Set<Double> toRemoveTimes = new HashSet<>();
        for (Double dropoffTime : dropOffTimes.keySet()) {
            if (dropoffTime <= getTimeNow()) {
                for (Entry<RoboTaxi, AVRequest> dropoffPair : dropOffTimes.get(dropoffTime).entrySet()) {

                    RoboTaxi roboTaxi = dropoffPair.getKey();
                    AVRequest avRequest = dropoffPair.getValue();

                    GlobalAssert.that(roboTaxi.getDivertableLocation().equals(avRequest.getToLink()));

                    roboTaxi.dropOffCustomer(); // This removes the dropoffCourse from the Menu
                    requestRegister.remove(roboTaxi, avRequest);
                    periodFulfilledRequests.put(avRequest, roboTaxi);
                    reqStatuses.remove(avRequest);
                    total_dropedOffRequests++;
                }
                toRemoveTimes.add(dropoffTime);
            }
        }
        toRemoveTimes.forEach(d -> dropOffTimes.remove(d));
    }

    /** ensures completed redirect tasks are removed from menu */
    @Override
    void executeRedirects() {
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            FinishRedirectionIfOnLastLink.now(roboTaxi);
        }
    }

    @Override
    /* package */ void stopAbortedPickupRoboTaxis() {
        // --- Deliberately empty
        // This is done in the redispatch internal function
    }

    /** called when a new request enters the system, adds request to
     * {@link pendingRequests}, needs to be public because called from other not
     * derived MATSim functions which are located in another package */
    @Override
    public final void onRequestSubmitted(AVRequest request) {
        boolean added = pendingRequests.add(request); // <- store request
        GlobalAssert.that(added);
        reqStatuses.put(request, RequestStatus.REQUESTED);
        periodSubmittdRequests.add(request);
    }

    /** Cleans menu for {@link RoboTaxi} and moves all previously assigned {@link AVRequest} back to pending requests taking them out from request- and pickup-
     * Registers. */
    /* package */ final void cleanAndAbondon(RoboTaxi roboTaxi) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        Objects.requireNonNull(roboTaxi);
        List<SharedCourse> oldCourses = roboTaxi.cleanAndAbandonMenu();
        oldCourses.stream().filter(sc -> !sc.getMealType().equals(SharedMealType.REDIRECT))//
                .forEach(sc -> {
                    pendingRequests.add(sc.getAvRequest());
                    reqStatuses.put(sc.getAvRequest(), RequestStatus.REQUESTED);
                    requestRegister.remove(roboTaxi, sc.getAvRequest());
                });
        GlobalAssert.that(!SharedCourseAccess.hasStarter(roboTaxi));
        GlobalAssert.that(!requestRegister.contains(roboTaxi));
    }

    /** Consistency checks to be called by
     * {@linksRoboTaxiMaintainer.consistencyCheck} in each iteration. */
    @Override
    protected final void consistencySubCheck() {

        // TODO disable or reduce computational complexity of entire subcheck once API tested for
        // a longer amount of time.

        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            Schedule schedule = roboTaxi.getSchedule();
            Task task = schedule.getCurrentTask();
            /** schedule should never have more than two elements on the next timestep */
            GlobalAssert.that(MaxTwoMoreTasksAfterEndingOne.check(schedule, task, getTimeNow(), SIMTIMESTEP));
            GlobalAssert.that(roboTaxi.getStatus().equals(SharedRoboTaxiUtils.calculateStatusFromMenu(roboTaxi)));
            Optional<SharedCourse> nextCourseOptional = SharedCourseAccess.getStarter(roboTaxi);
            if (nextCourseOptional.isPresent()) {
                if (nextCourseOptional.get().getMealType().equals(SharedMealType.REDIRECT)) {
                    if (roboTaxi.getMenuOnBoardCustomers() == 0) {
                        /** if a redirect meal is next and no customer on board, this is exactly
                         * a rebalcne drive and should be recorded accordingly. */
                        GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE));
                    }
                }
            }
            /** vice versa, if the {@link RoboTaxiStatus} is on REBALANCEDRIVE, it must
             * be on a redirect task. */
            if (roboTaxi.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE))
                GlobalAssert.that(SharedCourseAccess.getStarter(roboTaxi).get().getMealType().equals(SharedMealType.REDIRECT));
        }

        for (AVRequest avRequest : requestRegister.getAssignedAvRequests()) {
            GlobalAssert.that(reqStatuses.containsKey(avRequest));
            // TODO Shared this could be tested in a analysis in the simulation object.
            if (reqStatuses.get(avRequest).equals(RequestStatus.DRIVING)) {
                GlobalAssert.that(requestRegister.getAssignedRoboTaxi(avRequest).get().getStatus()//
                        .equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
            }
        }

        /** check that each Request only appears once in the Request Register */
        Set<AVRequest> uniqueAvRequests = new HashSet<>();
        for (Map<String, AVRequest> map : requestRegister.getRegister().values()) {
            for (AVRequest avRequest : map.values()) {
                if (uniqueAvRequests.contains(avRequest)) {
                    System.out.println("This AV Request Occured Twice in the request Register " + avRequest.getId().toString());
                    GlobalAssert.that(false);
                }
                uniqueAvRequests.add(avRequest);
            }
        }

        /** there cannot be more pickup than open requests */
        GlobalAssert.that(requestRegister.getAssignedPendingRequests(pendingRequests).size() <= pendingRequests.size());

        /** there cannot be more pickup vehicles than open requests */
        GlobalAssert.that(getRoboTaxiSubset(RoboTaxiStatus.DRIVETOCUSTOMER).size() <= pendingRequests.size());

        /** all {@link RoboTaxi} in the request Register must have a starter course */
        GlobalAssert.that(requestRegister.getRegister().keySet().stream().allMatch(SharedCourseAccess::hasStarter));

        /** containment check pickupRegisterFunction and pendingRequests */
        requestRegister.getPickupRegister(pendingRequests).keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

        /** if a request appears in a menu, it must be in the request register */
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            if (SharedCourseAccess.hasStarter(roboTaxi)) {
                for (SharedCourse course : roboTaxi.getUnmodifiableViewOfCourses()) {
                    if (!course.getMealType().equals(SharedMealType.REDIRECT)) {
                        String requestId = course.getCourseId();
                        Map<String, AVRequest> requests = requestRegister.get(roboTaxi);
                        GlobalAssert.that(requests.containsKey(requestId));
                    }
                }
            }
        }

        /** test: every request appears only 2 times, pickup and dropoff across all menus */
        List<String> requestsInMenus = new ArrayList<>();
        getRoboTaxis().stream().filter(rt -> SharedCourseAccess.hasStarter(rt)).forEach(//
                rtx -> SharedCourseUtil.getUniqueAVRequests(rtx.getUnmodifiableViewOfCourses())//
                        .forEach(r -> requestsInMenus.add(r.getId().toString())));
        Set<String> uniqueMenuRequests = new HashSet<>(requestsInMenus);
        GlobalAssert.that(uniqueMenuRequests.size() == requestsInMenus.size());

        /** request register equals the requests in the menu of each {@link RoboTaxi} */
        Set<String> uniqueRegisterRequests = new HashSet<>();
        requestRegister.getRegister().values().stream().forEach(m -> m.keySet().stream().forEach(s -> {
            uniqueRegisterRequests.add(s);
            if (!uniqueMenuRequests.contains(s)) {
                GlobalAssert.that(false);
            }
        }));
        GlobalAssert.that(uniqueRegisterRequests.size() == uniqueMenuRequests.size());

        /** on-board customers must equal total_matchedRequests - total_dropedOffRequests , this is comptuationally
         * very expensive and must be chagned eventually . */
        onboardPassengerCheck.now(total_matchedRequests, total_dropedOffRequests, getRoboTaxis());

    }

    /** save simulation data into {@link SimulationObject} for later analysis and
     * visualization and communicate to clients */
    @Override
    protected final void notifySimulationSubscribers(long round_now, StorageUtils storageUtils) {
        if (publishPeriod > 0 && round_now % publishPeriod == 0) {
            SimulationObjectCompiler simulationObjectCompiler = SimulationObjectCompiler.create( //
                    round_now, getInfoLine(), total_matchedRequests, db);

            simulationObjectCompiler.insertRequests(reqStatuses);
            simulationObjectCompiler.insertRequests(periodAssignedRequests, RequestStatus.ASSIGNED);
            simulationObjectCompiler.insertRequests(periodPickedUpRequests, RequestStatus.PICKUP);
            simulationObjectCompiler.insertRequests(periodFulfilledRequests.keySet(), RequestStatus.DROPOFF);
            simulationObjectCompiler.insertRequests(periodSubmittdRequests, RequestStatus.REQUESTED);

            /** insert {@link RoboTaxi}s */
            simulationObjectCompiler.insertVehicles(getRoboTaxis());

            /** insert information of association of {@link RoboTaxi}s and {@link AVRequest}s */
            Map<AVRequest, RoboTaxi> flatMap = new HashMap<>();
            requestRegister.getRegister().forEach((rt, map) -> map.values().forEach(avr -> flatMap.put(avr, rt)));
            periodFulfilledRequests.forEach((avr, rt) -> flatMap.put(avr, rt)); // adds the robotax for droped off requests (not in requestregister anymore)

            simulationObjectCompiler.addRequestRoboTaxiAssoc(flatMap);

            /** clear all the request Registers */
            periodAssignedRequests.clear();
            periodPickedUpRequests.clear();
            periodFulfilledRequests.clear();
            periodSubmittdRequests.clear();

            /** in the first pass, the vehicles are typically empty, then
             * {@link SimulationObject} is not stored or communicated */
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

    /** adding a vehicle during setup of simulation, handeled by {@link AVGenerator} */
    @Override
    public final void addVehicle(AVVehicle vehicle) {
        RoboTaxi roboTaxi = new RoboTaxi(vehicle, new LinkTimePair(vehicle.getStartLink(), 0.0), vehicle.getStartLink(), RoboTaxiUsageType.SHARED);
        Event event = new AVVehicleAssignmentEvent(vehicle, 0);
        addRoboTaxi(roboTaxi, event);
    }

    @Override
    protected final void updateDivertableLocations() {
        // Check that we really use the right SIMTime Step.
        // its done here as this function is calle before the step
        if (lastTime != null)
            GlobalAssert.that(SIMTIMESTEP == getTimeNow() - lastTime); // Make sure the hard coded Time step is chosen right
        lastTime = getTimeNow();

        // Update the divertable Location
        for (RoboTaxi robotaxi : getRoboTaxis()) {
            Schedule schedule = robotaxi.getSchedule();
            new RoboTaxiTaskAdapter(schedule.getCurrentTask()) {
                @Override
                public void handle(AVDriveTask avDriveTask) {
                    TaskTracker taskTracker = avDriveTask.getTaskTracker();
                    AmodeusDriveTaskTracker onlineDriveTaskTracker = (AmodeusDriveTaskTracker) taskTracker;
                    LinkTimePair linkTimePair = onlineDriveTaskTracker.getSafeDiversionPoint();
                    robotaxi.setDivertableLinkTime(linkTimePair); // contains null check
                    robotaxi.setCurrentDriveDestination(avDriveTask.getPath().getToLink());
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
                    if (ScheduleUtils.isLastTask(schedule, avStayTask) && !requestRegister.contains(robotaxi) && !periodFulfilledRequests.containsValue(robotaxi)) {
                        GlobalAssert.that(avStayTask.getBeginTime() <= getTimeNow());
                        GlobalAssert.that(avStayTask.getLink() != null);
                        robotaxi.setDivertableLinkTime(new LinkTimePair(avStayTask.getLink(), getTimeNow()));
                        robotaxi.setCurrentDriveDestination(avStayTask.getLink());
                        if (!SharedCourseAccess.hasStarter(robotaxi)) {
                            GlobalAssert.that(robotaxi.getStatus().equals(RoboTaxiStatus.STAY));
                        }
                    }
                }
            };
        }
    }

}
