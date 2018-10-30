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
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.TaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
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
import ch.ethz.matsim.av.schedule.AVTask;
import ch.ethz.matsim.av.schedule.AVTask.AVTaskType;

/** purpose of {@link SharedUniversalDispatcher} is to collect and manage
 * {@link AVRequest}s alternative implementation of {@link AVDispatcher};
 * supersedes {@link AbstractDispatcher}. */
public abstract class SharedUniversalDispatcher extends RoboTaxiMaintainer {
    // Registers for Simulation
    private final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
    private final Map<Double, Map<RoboTaxi, AVRequest>> dropOffTimes = new HashMap<>();
    // TODO Shared might be done with robotaxis only?
    private final RequestRegister requestRegister = new RequestRegister();

    // Registers for Simulation Objects
    private final Set<AVRequest> periodPickedUpRequests = new HashSet<>();
    private final Map<AVRequest, RoboTaxi> periodFulfilledRequests = new HashMap<>(); // A request is removed from the requestRegister at dropoff. So here we store the information
                                                                                      // from which Robotaxi it was droped off
    private final Set<AVRequest> periodAssignedRequests = new HashSet<>();
    private final Set<AVRequest> periodSubmittdRequests = new HashSet<>();
    private final Map<AVRequest, RequestStatus> reqStatuses = new HashMap<>(); // Storing the Request Statuses for the
                                                                               // SimObjects
    // Variables for consistency sub check
    private int total_matchedRequests = 0; // TODO Shared what is the use of this?
    private int total_dropedOffRequests = 0;//

    // Simulation Properties
    private final MatsimAmodeusDatabase db;
    private final FuturePathFactory futurePathFactory;
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;
    protected int publishPeriod; // not final, so that dispatchers can disable, or manipulate
    /*package*/ static final double SIMTIMESTEP = 1.0;// This is used in the Shared Universal Dispatcher to see if a task will end in the next timestep.
    private Double lastTime = null;

    protected SharedUniversalDispatcher( //
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
        // TODO make sure this is in correct and matches our needs and whishes of the behaviour of the redispatch
        roboTaxi.setStatus(RoboTaxiUtils.calculateStatusFromMenu(roboTaxi));
        GlobalAssert.that(RoboTaxiUtils.getRequestsInMenu(roboTaxi).contains(avRequest));
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
            roboTaxi.setStatus(RoboTaxiUtils.calculateStatusFromMenu(roboTaxi));
            GlobalAssert.that(RoboTaxiUtils.checkMenuConsistency(roboTaxi));
        } else {
            System.out.println("This place should not be reached");
            GlobalAssert.that(false);
        }
    }

    // ***********************************************************************************************
    // ********************* INTERNAL Methods, do not call from derived dispatchers*******************
    // ***********************************************************************************************

    /** carries out the redispatching defined in the {@link SharedMenu} and executes the
     * directives after a check of the menus. */
    @Override
    final void redispatchInternal(double now) {

        /** {@link RoboTaxi} are diverted which:
         *  are divertable 
         * a) if they have a starter:
         * - do not yet Plan to go to the link of this starter
         * b) if they do not have a starter but are on the way to a location they are stoped*/
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            SharedRoboTaxiDiversionHelper.adaptMenuToDirective(roboTaxi, futurePathFactory, now, eventsManager);
        }
    }

    /** Function called from {@link UniversalDispatcher.executePickups} if asRoboTaxi
     * scheduled for pickup has reached the from link of the {@link AVRequest}.
     * 
     * @paramsRoboTaxi
     * @param avRequest */
    private synchronized final void setAcceptRequest(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(RoboTaxiUtils.canPickupNewCustomer(roboTaxi));
        GlobalAssert.that(pendingRequests.contains(avRequest));
        Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());
        GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.PICKUP));
        GlobalAssert.that(currentCourse.get().getCourseId().equals(avRequest.getId().toString()));
        GlobalAssert.that(currentCourse.get().getLink().equals(avRequest.getFromLink()));
        GlobalAssert.that(currentCourse.get().getLink().equals(roboTaxi.getDivertableLocation()));
        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // check that current task is last task in schedule

        // Update the Robo Taxi
        roboTaxi.pickupNewCustomerOnBoard();
        roboTaxi.setStatus(RoboTaxiStatus.DRIVEWITHCUSTOMER); // has to be done here as this is required in the is without customer check
        roboTaxi.setCurrentDriveDestination(currentCourse.get().getLink());

        // Update the registers
        boolean checkPendingRemoved = pendingRequests.remove(avRequest);
        GlobalAssert.that(checkPendingRemoved);
        reqStatuses.put(avRequest, RequestStatus.DRIVING);
        periodPickedUpRequests.add(avRequest);
        ++total_matchedRequests;

        // Assign Directive
        final double endPickupTime = getTimeNow() + pickupDurationPerStop;
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getFromLink(), RoboTaxiUtils.getStarterLink(roboTaxi), endPickupTime);
        roboTaxi.assignDirective(new SharedGeneralPickupDirective(roboTaxi, avRequest, futurePathContainer, getTimeNow()));

        GlobalAssert.that(!roboTaxi.isDivertable());

        // After Function Checks
        GlobalAssert.that(!pendingRequests.contains(avRequest));
        GlobalAssert.that(!roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.pickupCourse(avRequest)));
        GlobalAssert.that(roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.dropoffCourse(avRequest)));
        GlobalAssert.that(requestRegister.contains(roboTaxi, avRequest));
    }

    /** Function called from {@link UniversalDispatcher.executeDropoffs} if
     * asRoboTaxi scheduled for dropoff has reached the from link of the
     * {@link AVRequest}.
     * 
     * @paramsRoboTaxi
     * @param avRequest */
    private synchronized final void setPassengerDropoff(RoboTaxi roboTaxi, AVRequest avRequest) {
        // CHECK That Dropoff Is Possible
        GlobalAssert.that(requestRegister.contains(roboTaxi, avRequest));
        Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());
        GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.DROPOFF));
        GlobalAssert.that(currentCourse.get().getCourseId().equals(avRequest.getId().toString()));
        GlobalAssert.that(currentCourse.get().getLink().equals(avRequest.getToLink()));
        GlobalAssert.that(currentCourse.get().getLink().equals(roboTaxi.getDivertableLocation()));
        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // instanceof AVDriveTask);

        // Assign Directive To roboTaxi
        final double endDropOffTime = getTimeNow() + dropoffDurationPerStop;
        Optional<SharedCourse> secondCourse = RoboTaxiUtils.getSecondCourse(roboTaxi);
        final Link endLink = (secondCourse.isPresent()) ? secondCourse.get().getLink() : avRequest.getToLink();
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getToLink(), endLink, endDropOffTime);
        roboTaxi.assignDirective(new SharedGeneralDropoffDirective(roboTaxi, avRequest, futurePathContainer, getTimeNow(), dropoffDurationPerStop));

        if (!dropOffTimes.containsKey(endDropOffTime)) {
            dropOffTimes.put(endDropOffTime, new HashMap<>());
        }
        dropOffTimes.get(endDropOffTime).put(roboTaxi, avRequest);

    }

    @Override
    /* package */ void stopAbortedPickupRoboTaxis() {
        /** stop vehicles still driving to a request but other taxi serving that request already */
        GlobalAssert.that(requestRegister.getAssignedPendingRequests(pendingRequests).size() <= pendingRequests.size());
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the
     * fromLink of an {@link AVRequest} */
    @Override
    void executePickups() {
        Map<AVRequest, RoboTaxi> pickupRegisterCopy = new HashMap<>(requestRegister.getPickupRegister(pendingRequests));
        List<RoboTaxi> pickupUniqueRoboTaxis = pickupRegisterCopy.values().stream() //
                .filter(srt -> RoboTaxiUtils.nextCourseIsOfType(srt, SharedMealType.PICKUP)) //
                .distinct() //
                .collect(Collectors.toList());
        for (RoboTaxi roboTaxi : pickupUniqueRoboTaxis) {
            // Same for dropoffs
            Link pickupVehicleLink = roboTaxi.getDivertableLocation();
            // SHARED note that waiting for last staytask adds a one second staytask before
            // switching to pickuptask
            boolean isOk = roboTaxi.getSchedule().getCurrentTask() == Schedules.getLastTask(roboTaxi.getSchedule()); // instanceof

            Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
            GlobalAssert.that(currentCourse.isPresent());
            AVRequest avR = currentCourse.get().getAvRequest();
            GlobalAssert.that(pendingRequests.contains(avR));
            GlobalAssert.that(requestRegister.contains(roboTaxi, avR));
            GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.PICKUP));

            if (avR.getFromLink().equals(pickupVehicleLink) && isOk) {
                setAcceptRequest(roboTaxi, avR);
            }
        }
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the toLink
     * of an {@link AVRequest} */
    @Override
    void executeDropoffs() {
        /** First the Tasks are assigned. This makes sure the dropoff takes place */
        Map<RoboTaxi, Map<String, AVRequest>> requestRegisterCopy = new HashMap<>(requestRegister.getRegister());
        for (RoboTaxi dropoffVehicle : requestRegisterCopy.keySet()) {
            Link dropoffVehicleLink = dropoffVehicle.getDivertableLocation();
            // SHARED note that waiting for last staytask adds a one second staytask before
            // switching to dropoffTask
            // This excludes as well that Requests are droped off a second time
            if (dropoffVehicle.getSchedule().getCurrentTask() == Schedules.getLastTask(dropoffVehicle.getSchedule())) {
                Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(dropoffVehicle);
                GlobalAssert.that(currentCourse.isPresent());
                if (currentCourse.get().getMealType().equals(SharedMealType.DROPOFF)) {
                    AVRequest avR = currentCourse.get().getAvRequest();

                    GlobalAssert.that(requestRegister.contains(dropoffVehicle, avR));

                    if (avR.getToLink().equals(dropoffVehicleLink) && //
                            dropoffVehicle.isWithoutDirective()) {
                        setPassengerDropoff(dropoffVehicle, avR);
                    }
                }
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
                    // TODO Shared update RoboTaxi Status: The RoboTaxiStatus could be executed as well from the menu internally
                    Optional<SharedCourse> nextCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
                    roboTaxi.setStatus(RoboTaxiUtils.calculateStatusFromMenu(roboTaxi));
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
            if (RoboTaxiUtils.hasNextCourse(roboTaxi)) {
                Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
                /** search redirect courses */
                if (currentCourse.isPresent()) {
                    if (currentCourse.get().getMealType().equals(SharedMealType.REDIRECT)) {
                        /** search if arrived at redirect destination */
                        if (currentCourse.get().getLink().equals(roboTaxi.getDivertableLocation())) {
                            roboTaxi.finishRedirection();
                            roboTaxi.setStatus(RoboTaxiUtils.calculateStatusFromMenu(roboTaxi));
                        }
                    }
                }
            }
        }
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
        GlobalAssert.that(!RoboTaxiUtils.hasNextCourse(roboTaxi));
        GlobalAssert.that(!requestRegister.contains(roboTaxi));
    }

    /** Consistency checks to be called by
     * {@linksRoboTaxiMaintainer.consistencyCheck} in each iteration. */
    @Override
    protected final void consistencySubCheck() {

        for (RoboTaxi roboTaxi : getRoboTaxis()) {

            // Check Shedule never has more than two elements on the nexttime step

            Schedule schedule = roboTaxi.getSchedule();
            Task task = schedule.getCurrentTask();
            GlobalAssert.that(SharedRoboTaxiDiversionHelper.maxTwoMoreTaskAfterThisOneWhichEnds(schedule, task, getTimeNow(), SIMTIMESTEP));

            GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiUtils.calculateStatusFromMenu(roboTaxi)));
            Optional<SharedCourse> nextCourseOptional = RoboTaxiUtils.getStarterCourse(roboTaxi);
            if (nextCourseOptional.isPresent()) {
                if (nextCourseOptional.get().getMealType().equals(SharedMealType.REDIRECT)) {
                    if (RoboTaxiUtils.getNumberOnBoardRequests(roboTaxi) == 0) {
                        GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE));
                    }
                }
            }

            if (roboTaxi.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE)) {
                GlobalAssert.that(RoboTaxiUtils.getStarterCourse(roboTaxi).get().getMealType().equals(SharedMealType.REDIRECT));
            }
        }

        for (AVRequest avRequest : requestRegister.getAssignedAvRequests()) {
            GlobalAssert.that(reqStatuses.containsKey(avRequest));
            // TODO Shared this could be tested in a analysis in the simulation object. There its crucial to be correct
            if (reqStatuses.get(avRequest).equals(RequestStatus.DRIVING)) {
                GlobalAssert.that(requestRegister.getAssignedRoboTaxi(avRequest).get().getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
            }
        }
        // TODO Shared this is important to work through again as this can save a lot of computatioinal effort

        // check that each Request only appears once in the Request Register
        Set<AVRequest> uniqueAvRequests = new HashSet<>();
        for (Entry<RoboTaxi, Map<String, AVRequest>> entry : requestRegister.getRegister().entrySet()) {
            for (AVRequest avRequest : entry.getValue().values()) {
                if (uniqueAvRequests.contains(avRequest)) {
                    System.out.println("This AV Request Occured Twice in the request Register " + avRequest.getId().toString());
                    GlobalAssert.that(false);
                }
                uniqueAvRequests.add(avRequest);
            }
        }

        // there cannot be more pickup requests than open requests
        GlobalAssert.that(requestRegister.getAssignedPendingRequests(pendingRequests).size() <= pendingRequests.size());

        // there cannot be more pickup vehicles than open requests
        GlobalAssert.that(getRoboTaxiSubset(RoboTaxiStatus.DRIVETOCUSTOMER).size() <= pendingRequests.size());

        // all Robotaxi in the request Register have a current course
        GlobalAssert.that(requestRegister.getRegister().keySet().stream().allMatch(RoboTaxiUtils::hasNextCourse));

        // containment check pickupRegisterFunction and pendingRequests
        requestRegister.getPickupRegister(pendingRequests).keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

        // check Menu consistency of each Robo Taxi
        getRoboTaxis().stream().filter(rt -> RoboTaxiUtils.hasNextCourse(rt)).forEach(rtx -> GlobalAssert.that(RoboTaxiUtils.checkMenuConsistency(rtx)));

        /** if a request appears in a menu, it must be in the request register */
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            if (RoboTaxiUtils.hasNextCourse(roboTaxi)) {
                for (SharedCourse course : roboTaxi.getUnmodifiableViewOfCourses()) {
                    if (!course.getMealType().equals(SharedMealType.REDIRECT)) {
                        String requestId = course.getCourseId();
                        Map<String, AVRequest> requests = requestRegister.get(roboTaxi);
                        GlobalAssert.that(requests.containsKey(requestId));
                    }
                }
            }
        }

        /** test: every request appears only 2 times, pickup and dropff accross all menus */
        List<String> requestsInMenus = new ArrayList<>();
        getRoboTaxis().stream().filter(rt -> RoboTaxiUtils.hasNextCourse(rt)).forEach(//
                rtx -> SharedCourseListUtils.getUniqueAVRequests(rtx.getUnmodifiableViewOfCourses()).forEach(r -> requestsInMenus.add(r.getId().toString())));
        Set<String> uniqueMenuRequests = new HashSet<>(requestsInMenus);
        GlobalAssert.that(uniqueMenuRequests.size() == requestsInMenus.size());

        /** request register equals the requests in the menu of each robo taxi */
        Set<String> uniqueRegisterRequests = new HashSet<>();
        requestRegister.getRegister().values().stream().forEach(m -> m.keySet().stream().forEach(s -> {
            uniqueRegisterRequests.add(s);
            if (!uniqueMenuRequests.contains(s)) {
                GlobalAssert.that(false);
            }
        }));
        GlobalAssert.that(uniqueRegisterRequests.size() == uniqueMenuRequests.size());

        /** check that the number of customers in vehicles equals
         * the number of picked up minus droped off customers. */
        GlobalAssert.that(total_matchedRequests - total_dropedOffRequests == getRoboTaxis().stream().mapToInt(rt -> RoboTaxiUtils.getNumberOnBoardRequests(rt)).sum());

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
            Map<AVRequest, RoboTaxi> map = new HashMap<>();
            // The normal case is when the AV Request is in the RequestRegister.
            for (RoboTaxi roboTaxi : requestRegister.getRegister().keySet())
                for (AVRequest avr : requestRegister.getRegister().get(roboTaxi).values())
                    map.put(avr, roboTaxi);
            // For the Dropped off customers the AV Request is not in the RequestRegister anymore. then the information is taken from the fulfilled requests
            periodFulfilledRequests.forEach((avr, rt) -> map.put(avr, rt));

            simulationObjectCompiler.addRequestRoboTaxiAssoc(map);

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
                        if (!RoboTaxiUtils.hasNextCourse(robotaxi)) {
                            robotaxi.setStatus(RoboTaxiStatus.STAY);
                        }
                    }
                }
            };
        }
    }

}
