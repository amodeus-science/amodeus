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

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
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
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** purpose of {@link SharedUniversalDispatcher} is to collect and manage
 * {@link AVRequest}s alternative implementation of {@link AVDispatcher};
 * supersedes {@link AbstractDispatcher}. */
public abstract class SharedUniversalDispatcher extends SharedRoboTaxiMaintainer {
    private final MatsimStaticDatabase db;

    private final FuturePathFactory futurePathFactory;
    private final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
    private final Map<AVRequest, RoboTaxi> pickupRegister = new HashMap<>(); // new RequestRegister
    private final Map<RoboTaxi, Map<String, AVRequest>> requestRegister = new HashMap<>();
    private final Set<AVRequest> periodPickedUpRequests = new HashSet<>(); // new
    private final Set<AVRequest> periodFulfilledRequests = new HashSet<>();
    private final Set<AVRequest> periodAssignedRequests = new HashSet<>();
    private final Set<AVRequest> periodSubmittdRequests = new HashSet<>();

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
            EventsManager eventsManager, //
            MatsimStaticDatabase db) {
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
    protected final Collection<RoboTaxi> getDivertableRoboTaxisWithoutCustomerOnBoard() {
        Collection<RoboTaxi> roboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> rt.isWithoutCustomer()) //
                .collect(Collectors.toList());
        GlobalAssert.that(roboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
        return roboTaxis;
    }

    /** @return divertablesRoboTaxis which currently not on a pickup drive */
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxis() {
        Collection<RoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> !requestRegister.containsKey(rt)) //
                .collect(Collectors.toList());
        GlobalAssert.that(!divertableUnassignedRoboTaxis.stream().anyMatch(requestRegister::containsKey));
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

    protected final Collection<RoboTaxi> getRoboTaxisWithAtLeastXFreeSeats(int x) {
        return getDivertableRoboTaxis().stream() //
                .filter(rt -> rt.getCapacity() - rt.getCurrentNumberOfCustomersOnBoard() >= x) //
                .collect(Collectors.toList());
    }

    /** @return immutable copy of pickupRegister, displays which vehicles are
     *         currently scheduled to pickup which request */
    protected final Map<AVRequest, RoboTaxi> getPickupRegister() {
        return Collections.unmodifiableMap(pickupRegister);
    }

    /** Function to assign a vehicle to a request. Only to be used in the redispatch function of shared dispatchers.
     * 
     * @param roboTaxi
     * @param avRequest */
    @SuppressWarnings("unlikely-arg-type")
    public void addSharedRoboTaxiPickup(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(roboTaxi.canPickupNewCustomer());
        GlobalAssert.that(pendingRequests.contains(avRequest));

        // If the request was already assigned remove it from this vehicle in the request register and update its menu;
        if (pickupRegister.containsKey(avRequest)) {
            // warning: unlikely-arg-type
            RoboTaxi oldRoboTaxi = pickupRegister.get(avRequest);
            AVRequest val = requestRegister.get(oldRoboTaxi).remove(avRequest.getId().toString());
            Objects.requireNonNull(val);

            oldRoboTaxi.removeAVRequestFromMenu(avRequest);

            if (oldRoboTaxi.getMenu().getStarterCourse() == null) {
                Map<String, AVRequest> val2 = requestRegister.remove(oldRoboTaxi);
                Objects.requireNonNull(val2);
            }
            GlobalAssert.that(oldRoboTaxi.checkMenuConsistency());
        }

        if (!requestRegister.containsKey(roboTaxi)) {
            requestRegister.put(roboTaxi, new HashMap<>());
        }

        if (!pickupRegister.containsKey(avRequest))
            periodAssignedRequests.add(avRequest);

        pickupRegister.put(avRequest, roboTaxi);
        requestRegister.get(roboTaxi).put(avRequest.getId().toString(), avRequest);
        roboTaxi.addAVRequestToMenu(avRequest);

        GlobalAssert.that(roboTaxi.getMenu().getUniqueAVRequests().contains(avRequest.getId().toString()));

        reqStatuses.put(avRequest, RequestStatus.ASSIGNED);

    }

    /** carries out the redispatching defined in the {@link SharedMenu} and executes the
     * directives after a check of the menus. */
    @Override
    final void redispatchInternal(double now) {

        /** to be implemented externally in the dispatchers */
        redispatch(now);

        /** {@link RoboTaxi} are diverted which:
         * - have a starter
         * - are not on the link of the starter
         * - are divertable */
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            GlobalAssert.that(roboTaxi.checkMenuConsistency());
            if (roboTaxi.getMenu().hasStarter()) {
                SharedCourse starter = roboTaxi.getMenu().getStarterCourse();
                if (!roboTaxi.getDivertableLocation().equals(starter.getLink())) {
                    if (roboTaxi.isDivertable()) {
                        Link destLink = getStarterLink(roboTaxi);
                        RoboTaxiStatus status = RoboTaxiStatus.REBALANCEDRIVE;
                        if (roboTaxi.getCurrentNumberOfCustomersOnBoard() > 0) {
                            status = RoboTaxiStatus.DRIVEWITHCUSTOMER;
                        } else if (starter.getMealType().equals(SharedMealType.PICKUP)) {
                            status = RoboTaxiStatus.DRIVETOCUSTOMER;
                        }
                        setRoboTaxiDiversion(roboTaxi, destLink, status);
                    }
                }
            }
        }
    }

    // ===================================================================================
    // INTERNAL Methods, do not call from derived dispatchers.

    /** Helper Function
     * 
     * @param sRoboTaxi
     * @return The Next Link based on the menu. */
    private static Link getStarterLink(RoboTaxi sRoboTaxi) {
        return sRoboTaxi.getMenu().getStarterCourse().getLink();
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
    /* package */ final void setRoboTaxiDiversion(RoboTaxi sRoboTaxi, Link destination, RoboTaxiStatus status) {
        // update Status Of Robo Taxi
        sRoboTaxi.setStatus(status);

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
        GlobalAssert.that(sRoboTaxi.getMenu().getStarterCourse().getRequestId().equals(avRequest.getId().toString()));
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

        consistencySubCheck();

        final Schedule schedule = sRoboTaxi.getSchedule();
        // check that current task is last task in schedule
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
    private synchronized final void setPassengerDropoff(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(requestRegister.get(roboTaxi).containsValue(avRequest));

        AVRequest val = requestRegister.get(roboTaxi).remove(avRequest.getId().toString());
        Objects.requireNonNull(val);

        // save avRequests which are matched for one publishPeriod to ensure no requests
        // are lost in the recording.
        periodFulfilledRequests.add(avRequest);

        final Schedule schedule = roboTaxi.getSchedule();
        // check that current task is last task in schedule
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // instanceof AVDriveTask);

        final double endDropOffTime = getTimeNow() + dropoffDurationPerStop;

        roboTaxi.dropOffCustomer();

        SharedCourse nextCourse = roboTaxi.getMenu().getStarterCourse();
        FuturePathContainer futurePathContainer = (nextCourse != null)
                ? futurePathFactory.createFuturePathContainer(avRequest.getToLink(), getStarterLink(roboTaxi), endDropOffTime)
                : futurePathFactory.createFuturePathContainer(avRequest.getToLink(), avRequest.getToLink(), endDropOffTime);
        roboTaxi.assignDirective(new SharedGeneralDriveDirectiveDropoff(roboTaxi, avRequest, futurePathContainer, getTimeNow(), dropoffDurationPerStop));

        if (nextCourse == null) {
            Map<String, AVRequest> val2 = requestRegister.remove(roboTaxi);
            Objects.requireNonNull(val2);
        }

        reqStatuses.remove(avRequest);
        total_dropedOffRequests++;
        if (requestRegister.containsKey(roboTaxi)) {
            GlobalAssert.that(!requestRegister.get(roboTaxi).containsKey(avRequest));
        }
    }

    @Override
    /* package */ final boolean isInPickupRegister(RoboTaxi sRoboTaxi) {
        return pickupRegister.containsValue(sRoboTaxi);
    }

    @Override
    /* package */ final boolean isInRequestRegister(RoboTaxi sRoboTaxi) {
        return requestRegister.containsKey(sRoboTaxi);
    }

    @Override
    /* package */ void stopAbortedPickupRoboTaxis() {
        GlobalAssert.that(true);
        // --
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
        List<RoboTaxi> pickupUniqueRoboTaxis = pickupRegisterCopy.values().stream() //
                .filter(srt -> srt.getMenu().getStarterCourse().getMealType().equals(SharedMealType.PICKUP)) //
                .distinct() //
                .collect(Collectors.toList());
        for (RoboTaxi roboTaxi : pickupUniqueRoboTaxis) {
            Link pickupVehicleLink = roboTaxi.getDivertableLocation();
            // SHARED note that waiting for last staytask adds a one second staytask before
            // switching to pickuptask
            boolean isOk = roboTaxi.getSchedule().getCurrentTask() == Schedules.getLastTask(roboTaxi.getSchedule()); // instanceof
            // AVDriveTask;
            // //

            SharedCourse currentCourse = roboTaxi.getMenu().getStarterCourse();
            AVRequest avR = requestRegister.get(roboTaxi).get(currentCourse.getRequestId());

            GlobalAssert.that(pendingRequests.contains(avR));
            GlobalAssert.that(pickupRegisterCopy.containsKey(avR));
            GlobalAssert.that(currentCourse.getMealType().equals(SharedMealType.PICKUP));

            if (avR.getFromLink().equals(pickupVehicleLink) && isOk) {
                setAcceptRequest(roboTaxi, avR);
            }
        }
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the toLink
     * of an {@link AVRequest} */
    @Override
    void executeDropoffs() {
        Map<RoboTaxi, Map<String, AVRequest>> requestRegisterCopy = new HashMap<>(requestRegister);
        for (RoboTaxi dropoffVehicle : requestRegisterCopy.keySet()) {
            Link dropoffVehicleLink = dropoffVehicle.getDivertableLocation();
            // SHARED note that waiting for last staytask adds a one second staytask before
            // switching to dropoffTask
            boolean isOk = dropoffVehicle.getSchedule().getCurrentTask() == Schedules.getLastTask(dropoffVehicle.getSchedule()); // instanceof AVDriveTask;

            SharedCourse currentCourse = dropoffVehicle.getMenu().getStarterCourse();
            Objects.requireNonNull(currentCourse);

            AVRequest avR = requestRegister.get(dropoffVehicle).get(currentCourse.getRequestId());

            if (currentCourse.getMealType().equals(SharedMealType.DROPOFF) && //
                    avR.getToLink().equals(dropoffVehicleLink) && //
                    dropoffVehicle.isWithoutDirective() && //
                    isOk) {
                setPassengerDropoff(dropoffVehicle, avR);
            }
        }
    }

    /** ensures completed redirect tasks are removed from menu */
    @Override
    void executeRedirects() {
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            SharedCourse currentCourse = roboTaxi.getMenu().getStarterCourse();
            /** search redirect courses */
            if (Objects.nonNull(currentCourse)) {
                if (currentCourse.getMealType().equals(SharedMealType.REDIRECT)) {
                    /** search if arrived at redirect destination */
                    if (currentCourse.getLink().equals(roboTaxi.getDivertableLocation())) {
                        roboTaxi.finishRedirection();
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
        roboTaxi.getMenu().clearWholeMenu();
        Objects.requireNonNull(requestRegister);
        if (requestRegister.containsKey(roboTaxi)) {
            requestRegister.get(roboTaxi).entrySet().stream().forEach(entry -> {
                pendingRequests.add(entry.getValue());
                reqStatuses.put(entry.getValue(), RequestStatus.REQUESTED);
                pickupRegister.remove(entry.getValue());
            });
            Map<String, AVRequest> val = requestRegister.remove(roboTaxi);
            Objects.requireNonNull(val);
        }
        GlobalAssert.that(!roboTaxi.getMenu().hasStarter());
        GlobalAssert.that(!requestRegister.containsKey(roboTaxi));
        GlobalAssert.that(!pickupRegister.containsValue(roboTaxi));
    }

    /** Consistency checks to be called by
     * {@linksRoboTaxiMaintainer.consistencyCheck} in each iteration. */
    @Override
    protected final void consistencySubCheck() {
        Set<AVRequest> uniqueAvRequests = new HashSet<>();
        for (Entry<RoboTaxi, Map<String, AVRequest>> entry : requestRegister.entrySet()) {
            for (AVRequest avRequest : entry.getValue().values()) {
                if (uniqueAvRequests.contains(avRequest)) {
                    System.out.println("This AV Request Occured Twice in the request Register " + avRequest.getId().toString());
                    GlobalAssert.that(false);
                }
                uniqueAvRequests.add(avRequest);
            }

        }

        // there cannot be more pickup vehicles than open requests
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());

        // all Robotaxi in the request Register have a current course
        requestRegister.keySet().forEach(roboTaxi -> GlobalAssert.that(roboTaxi.getMenu().getStarterCourse() != null));

        requestRegister.forEach((k, v) -> GlobalAssert.that(k.getMenu().getStarterCourse() != null));

        // pickupRegister needs to be a subset of requestRegister
        pickupRegister.forEach((k, v) -> GlobalAssert.that(requestRegister.get(v).containsValue(k)));

        // containment check pickupRegister and pendingRequests
        pickupRegister.keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

        // check Menu consistency of each Robo Taxi
        getRoboTaxis().stream().filter(rt -> rt.getMenu().hasStarter()).forEach(rtx -> GlobalAssert.that(rtx.checkMenuConsistency()));

        /** if a request appears in a menu, it must be in the request register */
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            if (roboTaxi.getMenu().hasStarter()) {
                for (SharedCourse course : roboTaxi.getMenu().getCourses()) {
                    if (!course.getMealType().equals(SharedMealType.REDIRECT)) {
                        String requestId = course.getRequestId();
                        Map<String, AVRequest> requests = requestRegister.get(roboTaxi);
                        GlobalAssert.that(requests.containsKey(requestId));
                    }
                }
            }
        }

        /** test: every request appears only 2 times, pickup and dropff accross all menus */
        List<String> requestsInMenus = new ArrayList<>();
        getRoboTaxis().stream().filter(rt -> rt.getMenu().hasStarter()).forEach(//
                rtx -> rtx.getMenu().getUniqueAVRequests().forEach(id -> requestsInMenus.add(id)));
        Set<String> uniqueMenuRequests = new HashSet<>(requestsInMenus);
        GlobalAssert.that(uniqueMenuRequests.size() == requestsInMenus.size());

        /** request register equals the requests in the menu of each robo taxi */
        Set<String> uniqueRegisterRequests = new HashSet<>();
        requestRegister.values().stream().forEach(m -> m.keySet().stream().forEach(s -> {
            uniqueRegisterRequests.add(s);
            if (!uniqueMenuRequests.contains(s)) {
                GlobalAssert.that(false);
            }
        }));
        GlobalAssert.that(uniqueRegisterRequests.size() == uniqueMenuRequests.size());

        /** check that the number of customers in vehicles equals
         * the number of picked up minus droped off customers. */
        GlobalAssert.that(total_matchedRequests - total_dropedOffRequests == getRoboTaxis().stream().mapToInt(rt -> rt.getCurrentNumberOfCustomersOnBoard()).sum());

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
            simulationObjectCompiler.insertRequests(periodFulfilledRequests, RequestStatus.DROPOFF);
            simulationObjectCompiler.insertRequests(periodSubmittdRequests, RequestStatus.REQUESTED);

            periodAssignedRequests.clear();
            periodPickedUpRequests.clear();
            periodFulfilledRequests.clear();
            periodSubmittdRequests.clear();

            /** insert {@link RoboTaxi}s */
            simulationObjectCompiler.insertVehicles(getRoboTaxis());

            /** insert information of association of {@link RoboTaxi}s and {@link AVRequest}s */
            Map<AVRequest, RoboTaxi> map = new HashMap<>();
            for (RoboTaxi roboTaxi : requestRegister.keySet()) {
                for (AVRequest avr : requestRegister.get(roboTaxi).values()) {
                    map.put(avr, roboTaxi);
                }
            }
            simulationObjectCompiler.addRequestRoboTaxiAssoc(map);

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

}
