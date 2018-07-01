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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVMenu;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.SharedSimulationObjectCompiler;
import ch.ethz.idsc.amodeus.net.SimulationDistribution;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.SimulationObjects;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** purpose of {@link SharedUniversalDispatcher} is to collect and manage
 * {@link AVRequest}s alternative implementation of {@link AVDispatcher};
 * supersedes {@link AbstractDispatcher}. */
public abstract class SharedUniversalDispatcher extends SharedRoboTaxiMaintainer {

    private final FuturePathFactory futurePathFactory;
    private final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
    private final Map<AVRequest, RoboTaxi> pickupRegister = new HashMap<>(); // new RequestRegister
    private final Map<RoboTaxi, Map<Id<Request>, AVRequest>> requestRegister = new HashMap<>();
    private final Set<AVRequest> periodPickedUpRequests = new HashSet<>(); // new
    private final Set<AVRequest> periodFulfilledRequests = new HashSet<>(); // new
                                                                            // temporaryRequestRegister
                                                                            // for fulfilled requests
    private final Map<AVRequest, RequestStatus> reqStatuses = new HashMap<>(); // Storing the Request Statuses for the
                                                                               // SimObjects
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;
    protected int publishPeriod; // not final, so that dispatchers can disable, or manipulate
    private int total_matchedRequests = 0;
    private int total_dropedOffRequests = 0;

    protected SharedUniversalDispatcher( //
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

    /** @return {@Collection} of all {@AVRequests} which are currently open. Requests
     *         are removed from list in setAcceptRequest function. */
    protected synchronized final Collection<AVRequest> getAVRequests() {
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
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxisWithoutCustomer() {
        Collection<RoboTaxi> roboTaxis = getDivertableUnassignedRoboTaxis().stream() //
                .filter(rt -> rt.isWithoutCustomer()) //
                .collect(Collectors.toList());
        GlobalAssert.that(!roboTaxis.stream().anyMatch(pickupRegister::containsValue));
        GlobalAssert.that(roboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
        return roboTaxis;
    }

    /** @return divertablesRoboTaxis which currently not on a pickup drive */
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxis() {
        Collection<RoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> !pickupRegister.containsValue(rt)) //
                .collect(Collectors.toList());
        GlobalAssert.that(!divertableUnassignedRoboTaxis.stream().anyMatch(pickupRegister::containsValue));
        // GlobalAssert.that(divertableUnassignedRoboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
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

    protected final Collection<RoboTaxi> getRoboTaxisWithAtLeastXFreeSeats(int x) {
        return getDivertableRoboTaxis().stream() //
                .filter(rt -> rt.hasAtLeastXSeatsFree(x)) //
                .collect(Collectors.toList());
    }

    /** @return immutable copy of pickupRegister, displays which vehicles are
     *         currently scheduled to pickup which request */
    protected final Map<AVRequest, RoboTaxi> getPickupRegister() {
        return Collections.unmodifiableMap(pickupRegister);
    }

    /** @return immutable copy of the requestRegister which is the register of all AV
     *         request for each Shared Robo Taxi */
    protected final Map<RoboTaxi, Map<Id<Request>, AVRequest>> getRequestRegister() {
        return Collections.unmodifiableMap(requestRegister);
    }

    public void addSharedRoboTaxiPickup(RoboTaxi sRoboTaxi, AVRequest avRequest) {
        GlobalAssert.that(sRoboTaxi.canPickupNewCustomer());
        GlobalAssert.that(pendingRequests.contains(avRequest));

        if (!requestRegister.containsKey(sRoboTaxi)) {
            requestRegister.put(sRoboTaxi, new HashMap<>());
        }
        // 1) enter information into pickup table
        // 2) also do everything for the full-time requestRegister
        if (pickupRegister.containsKey(avRequest))
            requestRegister.get(pickupRegister.get(avRequest)).remove(avRequest.getId());

        pickupRegister.put(avRequest, sRoboTaxi);
        requestRegister.get(sRoboTaxi).put(avRequest.getId(), avRequest);

        sRoboTaxi.getMenu().addAVCourseAsDessert(new SharedAVCourse(avRequest.getId(), SharedAVMealType.PICKUP));
        sRoboTaxi.getMenu().addAVCourseAsDessert(new SharedAVCourse(avRequest.getId(), SharedAVMealType.DROPOFF));

        reqStatuses.put(avRequest, RequestStatus.ASSIGNED);

    }

    /** carries out the redispatching defined in the redispatch and executes the
     * directives after a check of the menus. */
    @Override
    final void redispatchInternal(double now) {

        Map<RoboTaxi, SharedAVMenu> sharedAvMenuLastStep = new HashMap<>();
        getRoboTaxis().forEach(rt -> sharedAvMenuLastStep.put(rt, rt.getMenu().copy()));

        // To be implemented externally in the dispatchers
        redispatch(now);

        for (RoboTaxi sharedRoboTaxi : getRoboTaxis()) {
            GlobalAssert.that(sharedRoboTaxi.checkMenuConsistency());
            if (!sharedRoboTaxi.getMenu().equals(sharedAvMenuLastStep.get(sharedRoboTaxi))) { // If the menu changed compared to the last Time Step
                // As the menu changed set a diversion of the robotaxi based on the menu.
                RoboTaxiStatus avStatus = null;
                if (sharedRoboTaxi.getMenu().getStarterCourse().getPickupOrDropOff().equals(SharedAVMealType.PICKUP)) {
                    avStatus = sharedRoboTaxi.getCurrentNumberOfCustomersOnBoard() > 0 ? RoboTaxiStatus.DRIVEWITHCUSTOMER : RoboTaxiStatus.DRIVETOCUSTOMER;
                    GlobalAssert.that(sharedRoboTaxi.canPickupNewCustomer()); // TODO SHARED Check
                } else {
                    avStatus = RoboTaxiStatus.DRIVEWITHCUSTOMER;
                }
                Link destLink = getStarterLink(sharedRoboTaxi);
                setRoboTaxiDiversion(sharedRoboTaxi, destLink, avStatus);
            }
        }

    }

    // ===================================================================================
    // INTERNAL Methods, do not call from derived dispatchers.

    /** Helper Function
     * 
     * @param sRoboTaxi
     * @return The Next Link based on the menu and the request Register. */
    private Link getStarterLink(RoboTaxi sRoboTaxi) {
        SharedAVCourse course = sRoboTaxi.getMenu().getStarterCourse();
        AVRequest avR = requestRegister.get(sRoboTaxi).get(course.getRequestId());
        if (course.getPickupOrDropOff().equals(SharedAVMealType.PICKUP)) {
            return avR.getFromLink();
        } else if (course.getPickupOrDropOff().equals(SharedAVMealType.DROPOFF)) {
            return avR.getToLink();
        } else {
            throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
        }
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
     * @param avstatus
     *            {@link} the {@link AVStatus} the {@link RoboTaxi} has after
     *            the diversion, depends if used from {@link setRoboTaxiPickup} or
     *            {@link setRoboTaxiRebalance} */
    /* package */ final void setRoboTaxiDiversion(RoboTaxi sRoboTaxi, Link destination, RoboTaxiStatus avstatus) {
        // update Status Of Robo Taxi
        sRoboTaxi.setStatus(avstatus);

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

                    sRoboTaxi.assignDirective(new SharedGeneralStayDirective(sRoboTaxi, destination, futurePathContainer, getTimeNow()));

                } else
                    sRoboTaxi.assignDirective(EmptyDirective.INSTANCE);
            }

            @Override
            public void handle(AVPickupTask avPickupTask) {
                handlePickupAndDropoff(sRoboTaxi, task);
            }

            @Override
            public void handle(AVDropoffTask dropOffTask) {
                handlePickupAndDropoff(sRoboTaxi, task);
            }

            // FIXME This is probably not yet Good
            private void handlePickupAndDropoff(RoboTaxi sRoboTaxi, Task task) {
                Link nextLink = getStarterLink(sRoboTaxi); // We are already at next course in the menu although in
                // matsim the pickup or dropoff is still happening
                GlobalAssert.that(nextLink.equals(destination));
                FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                        sRoboTaxi.getDivertableLocation(), nextLink, task.getEndTime());

                sRoboTaxi.assignDirective(new SharedGeneralPickupOrDropoffDiversionDirective(sRoboTaxi, futurePathContainer, getTimeNow()));
            }
        };
    }

    /** Function called from {@link UniversalDispatcher.executePickups} if asRoboTaxi
     * scheduled for pickup has reached the from link of the {@link AVRequest}.
     * 
     * @paramsRoboTaxi
     * @param avRequest */
    private synchronized final void setAcceptRequest(RoboTaxi sRoboTaxi, AVRequest avRequest) {
        GlobalAssert.that(sRoboTaxi.canPickupNewCustomer());
        GlobalAssert.that(sRoboTaxi.getMenu().getStarterCourse().getRequestId().equals(avRequest.getId()));
        Link pickupLink = getStarterLink(sRoboTaxi);
        GlobalAssert.that(avRequest.getFromLink().equals(pickupLink));
        {
            boolean statusPen = pendingRequests.remove(avRequest);
            GlobalAssert.that(statusPen);
        }
        {
            RoboTaxi former = pickupRegister.remove(avRequest);
            GlobalAssert.that(sRoboTaxi == former);
        }
        sRoboTaxi.setStatus(RoboTaxiStatus.DRIVEWITHCUSTOMER);
        reqStatuses.put(avRequest, RequestStatus.DRIVING);
        periodPickedUpRequests.add(avRequest);
        // TODO SHARED Why are we doing this exactly here?
        consistencySubCheck();

        final Schedule schedule = sRoboTaxi.getSchedule();
        // check that current task is last task in schedule
        // TODO SHARED fix
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // instanceof AVDriveTask);

        final double endPickupTime = getTimeNow() + pickupDurationPerStop;

        // Remove pickup from menu
        sRoboTaxi.pickupNewCustomerOnBoard();
        // has to be after the remove of the menue which is done in the pick up new customer function
        sRoboTaxi.setCurrentDriveDestination(getStarterLink(sRoboTaxi));
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getFromLink(), getStarterLink(sRoboTaxi), endPickupTime);
        sRoboTaxi.assignDirective(new SharedGeneralDriveDirectivePickup(sRoboTaxi, avRequest, futurePathContainer, getTimeNow()));

        ++total_matchedRequests;
    }

    /** Function called from {@link UniversalDispatcher.executeDropoffs} if
     * asRoboTaxi scheduled for dropoff has reached the from link of the
     * {@link AVRequest}.
     * 
     * @paramsRoboTaxi
     * @param avRequest */
    private synchronized final void setPassengerDropoff(RoboTaxi sRoboTaxi, AVRequest avRequest) {
        GlobalAssert.that(requestRegister.get(sRoboTaxi).containsValue(avRequest));

        requestRegister.get(sRoboTaxi).remove(avRequest.getId());

        // save avRequests which are matched for one publishPeriod to ensure no requests
        // are lost in the recording.
        periodFulfilledRequests.add(avRequest);

        final Schedule schedule = sRoboTaxi.getSchedule();
        // check that current task is last task in schedule
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // instanceof AVDriveTask);

        final double endDropOffTime = getTimeNow() + dropoffDurationPerStop;

        sRoboTaxi.dropOffCustomer();

        SharedAVCourse nextCourse = sRoboTaxi.getMenu().getStarterCourse();
        FuturePathContainer futurePathContainer = (nextCourse != null)
                ? futurePathFactory.createFuturePathContainer(avRequest.getToLink(), getStarterLink(sRoboTaxi), endDropOffTime)
                : futurePathFactory.createFuturePathContainer(avRequest.getToLink(), avRequest.getToLink(), endDropOffTime);
        sRoboTaxi.assignDirective(new SharedGeneralDriveDirectiveDropoff(sRoboTaxi, avRequest, futurePathContainer, getTimeNow(), dropoffDurationPerStop));

        // FIXME temporary fix.. else it fails in execute dropoffs since there are
        // robotaxis in map but with no requests assigned.
        if (nextCourse == null)
            requestRegister.remove(sRoboTaxi);

        reqStatuses.remove(avRequest);
        total_dropedOffRequests++;
    }

    @Override
    /* package */ final boolean isInPickupRegister(RoboTaxi sRoboTaxi) {
        return pickupRegister.containsValue(sRoboTaxi);
    }

    @Override
    /* package */ final boolean isInRequestRegister(RoboTaxi sRoboTaxi) {
        return requestRegister.containsKey(sRoboTaxi);
    }

    /** @param avRequest
     * @returnsRoboTaxi assigned to given avRequest, or empty if no taxi is assigned
     *                  to avRequest Used by BipartiteMatching in
     *                  euclideanNonCyclic, there a comparison to the old av
     *                  assignment is needed */
    public final Optional<RoboTaxi> getPickupTaxi(AVRequest avRequest) {
        return Optional.ofNullable(pickupRegister.get(avRequest));
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the
     * fromLink of an {@link AVRequest} */
    @Override
    void executePickups() {
        Map<AVRequest, RoboTaxi> pickupRegisterCopy = new HashMap<>(pickupRegister);
        // TODO Ian is there any way of getting unique map values in a more efficient
        // way
        Set<RoboTaxi> uniqueRt = new HashSet<>();
        pickupRegisterCopy.values().stream().filter(srt -> srt.getMenu().getStarterCourse().getPickupOrDropOff().equals(SharedAVMealType.PICKUP)).forEach(rt -> uniqueRt.add(rt));
        for (RoboTaxi sRt : uniqueRt) {
            Link pickupVehicleLink = sRt.getDivertableLocation();
            // TODO note that waiting for last staytask adds a one second staytask before
            // switching to pickuptask
            boolean isOk = sRt.getSchedule().getCurrentTask() == Schedules.getLastTask(sRt.getSchedule()); // instanceof
                                                                                                           // AVDriveTask;
                                                                                                           // //

            SharedAVCourse currentCourse = sRt.getMenu().getStarterCourse();
            AVRequest avR = requestRegister.get(sRt).get(currentCourse.getRequestId());

            GlobalAssert.that(pendingRequests.contains(avR));
            GlobalAssert.that(pickupRegisterCopy.containsKey(avR));
            GlobalAssert.that(currentCourse.getPickupOrDropOff().equals(SharedAVMealType.PICKUP));

            if (avR.getFromLink().equals(pickupVehicleLink) && isOk) {
                setAcceptRequest(sRt, avR);
            }
        }
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the toLink
     * of an {@link AVRequest} */
    @Override
    void executeDropoffs() {
        Map<RoboTaxi, Map<Id<Request>, AVRequest>> requestRegisterCopy = new HashMap<>(requestRegister);
        for (RoboTaxi dropoffVehicle : requestRegisterCopy.keySet()) {
            Link dropoffVehicleLink = dropoffVehicle.getDivertableLocation();
            // TODO note that waiting for last staytask adds a one second staytask before
            // switching to dropoffTask
            boolean isOk = dropoffVehicle.getSchedule().getCurrentTask() == Schedules.getLastTask(dropoffVehicle.getSchedule()); // instanceof AVDriveTask;

            SharedAVCourse currentCourse = dropoffVehicle.getMenu().getStarterCourse();
            AVRequest avR = requestRegister.get(dropoffVehicle).get(currentCourse.getRequestId());

            if (currentCourse.getPickupOrDropOff().equals(SharedAVMealType.DROPOFF) && //
                    avR.getToLink().equals(dropoffVehicleLink) && //
                    dropoffVehicle.isWithoutDirective() && //
                    isOk) {
                setPassengerDropoff(dropoffVehicle, avR);
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
    }

    /** function stops {@link RoboTaxi} which are still heading towards an
     * {@link AVRequest} but another {@link RoboTaxi} was scheduled to pickup
     * this {@link AVRequest} in the meantime */
    @Override
    /* package */ final void stopAbortedPickupRoboTaxis() {
        // FIXME This function has to be checked. might not even be nesscesary anymore...
        // stop vehicles still driving to a request but other taxi serving that request
        // already
        // FIXME this is not nesscesary true for shared taxis It is possible that a robotaxi is on a
        // dropofftrip when the request is canceled..
        getRoboTaxis().stream()//
                .filter(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER)).filter(rt -> !pickupRegister.containsValue(rt))//
                .filter(RoboTaxi::canPickupNewCustomer)//
                .filter(RoboTaxi::isWithoutDirective)//
                .forEach(rt -> setRoboTaxiDiversion(rt, rt.getDivertableLocation(), RoboTaxiStatus.REBALANCEDRIVE));
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());
    }

    /** Cleans menu for {@link RoboTaxi} and moves all previously assigned {@link AVRequest} back to pending requests taking them out from request- and pickup-
     * Registers. */
    /* package */ final void cleanRoboTaxiMenuAndAbandonAssignedRequests(RoboTaxi roboTaxi) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        roboTaxi.getMenu().clearWholeMenu();
        requestRegister.get(roboTaxi).entrySet().stream().forEach(entry -> {
            pendingRequests.add(entry.getValue());
            reqStatuses.put(entry.getValue(), RequestStatus.REQUESTED);
            pickupRegister.remove(entry.getValue());
        });
        requestRegister.remove(roboTaxi);
        GlobalAssert.that(!roboTaxi.getMenu().hasStarter());
        GlobalAssert.that(!requestRegister.containsKey(roboTaxi));
        GlobalAssert.that(!pickupRegister.containsValue(roboTaxi));
    }

    /** Consistency checks to be called by
     * {@linksRoboTaxiMaintainer.consistencyCheck} in each iteration. */
    @Override
    protected final void consistencySubCheck() {
        // TODO SHARED checked
        // there cannot be more pickup vehicles than open requests
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());

        // pickupRegister needs to be a subset of requestRegister
        pickupRegister.forEach((k, v) -> GlobalAssert.that(requestRegister.get(v).containsValue(k)));

        // containment check pickupRegister and pendingRequests
        pickupRegister.keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

        // check Menu consistency of each Robo Taxi
        getRoboTaxis().stream().filter(rt -> rt.getMenu().hasStarter()).forEach(rtx -> GlobalAssert.that(rtx.checkMenuConsistency()));

        // TODO SHARED check statement below: menus requests are contained in request register.
        GlobalAssert.that(!getRoboTaxis().stream().filter(rt -> rt.getMenu().hasStarter())
                .anyMatch(rtx -> rtx.getMenu().getCourses().stream().anyMatch(c -> !requestRegister.get(rtx).containsKey(c.getRequestId()))));

        // Menus do not Contain duplicate requests
        List<Id<Request>> menusRequests = new ArrayList<>();
        getRoboTaxis().stream().filter(rt -> rt.getMenu().hasStarter()).forEach(rtx -> rtx.getMenu().getUniqueAVRequests().forEach(id -> menusRequests.add(id)));
        Set<Id<Request>> uniqueMenuReqests = (new HashSet<>(menusRequests));
        GlobalAssert.that(uniqueMenuReqests.size() == menusRequests.size());

        // check that the request register equals the requests in the menu of each robo taxi
        requestRegister.forEach((rt, map) -> rt.getMenu().getUniqueAVRequests().equals(new HashSet<>(map.values())));
        Set<Id<Request>> uniqueRequestRegisterReqests = new HashSet<>();
        requestRegister.forEach((rt, map) -> map.keySet().forEach(id -> uniqueRequestRegisterReqests.add(id)));
        GlobalAssert.that(uniqueRequestRegisterReqests.equals(uniqueMenuReqests));

        // check that the number of customers in vehicles equals the number of picked up minus droped off customers.
        GlobalAssert.that(total_matchedRequests - total_dropedOffRequests == getRoboTaxis().stream().mapToInt(rt -> rt.getCurrentNumberOfCustomersOnBoard()).sum());

    }

    /** save simulation data into {@link SimulationObject} for later analysis and
     * visualization. */
    @Override
    protected final void notifySimulationSubscribers(long round_now, StorageUtils storageUtils) {
        if (publishPeriod > 0 && round_now % publishPeriod == 0) {
            SharedSimulationObjectCompiler simulationObjectCompiler = SharedSimulationObjectCompiler.create( //
                    round_now, getInfoLine(), total_matchedRequests);

            simulationObjectCompiler.insertRequests(reqStatuses);
            simulationObjectCompiler.insertFulfilledRequests(periodFulfilledRequests);
            periodFulfilledRequests.clear();
            simulationObjectCompiler.insertPickedUpRequests(periodPickedUpRequests);
            periodPickedUpRequests.clear();

            simulationObjectCompiler.insertVehicles(getRoboTaxis());
            SimulationObject simulationObject = simulationObjectCompiler.compile();

            // in the first pass, the vehicles is typically empty
            // in that case, the simObj will not be stored or communicated
            if (SimulationObjects.hasVehicles(simulationObject)) {
                // store simObj and distribute to clients
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

}
