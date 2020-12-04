/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusGenerator;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.passenger.PassengerRequestScheduledEvent;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.schedule.Task.TaskStatus;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.Time;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.DriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;
import amodeus.amodeus.dispatcher.shared.Compatibility;
import amodeus.amodeus.dispatcher.shared.backup.SharedMenu;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.SimulationObjectCompiler;
import amodeus.amodeus.util.math.GlobalAssert;

/** purpose of {@link SharedUniversalDispatcher} is to collect and manage
 * {@link PassengerRequest}s alternative implementation of {@link AmodeusDispatcher};
 * supersedes {@link AbstractDispatcher}. */
public abstract class SharedUniversalDispatcher extends BasicUniversalDispatcher {
    /** contains all Requests which are not picked Up Yet */
    // private final Map<Double, Map<RoboTaxi, PassengerRequest>> dropOffTimes = new HashMap<>();
    // private final Map<Double, Map<RoboTaxi, Set<PassengerRequest>>> pickUpTimes = new HashMap<>();

    private final Map<RoboTaxi, AmodeusStopTask> pickupTaxis = new HashMap<>();
    private final Map<RoboTaxi, AmodeusStopTask> dropoffTaxis = new HashMap<>();

    private final RequestRegister requestRegister = new RequestRegister();
    /** contains all Requests which are assigned to a RoboTaxi */

    private final Set<RoboTaxi> timeStepReroute = new HashSet<>();

    // Registers for Simulation Objects
    private final Set<PassengerRequest> periodPickedUpRequests = new HashSet<>();
    // A request is removed from the requestRegister at dropoff. So here we store the information from which robotaxi it was dropped off
    private final Map<PassengerRequest, RoboTaxi> periodFulfilledRequests = new HashMap<>();
    private final Set<PassengerRequest> periodAssignedRequests = new HashSet<>();
    private final Set<PassengerRequest> periodSubmittdRequests = new HashSet<>();
    private final Map<PassengerRequest, RequestStatus> reqStatuses = new HashMap<>(); // Storing the Request Statuses for the SimObjects
    // Variables for consistency sub check
    private int total_dropedOffRequests = 0;//

    private final OnboardPassengerCheck onboardPassengerCheck = //
            new OnboardPassengerCheck(total_matchedRequests, total_dropedOffRequests);

    /* package */ static final double SIMTIMESTEP = 1.0; // This is used in the Shared Universal Dispatcher to see if a task will end in the next timestep.
    private Double lastTime = null;

    protected SharedUniversalDispatcher(Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            EventsManager eventsManager, MatsimAmodeusDatabase db) {
        super(eventsManager, config, operatorConfig, travelTime, parallelLeastCostPathCalculator, db);
    }

    // ===================================================================================
    // Methods to use EXTERNALLY in derived dispatchers

    /** @return PassengerRequests which are currently not assigned to a vehicle */
    protected synchronized final List<PassengerRequest> getUnassignedPassengerRequests() {
        return pendingRequests.stream() //
                .filter(r -> !requestRegister.contains(r)) //
                .collect(Collectors.toList());
    }

    /** @return {@link Map} of {@link PassengerRequest}s which have an assigned {@link RoboTaxi}
     *         but are not Picked up yet. Associated value is the corresponding {@link RoboTaxi} */
    protected final Map<PassengerRequest, RoboTaxi> getCurrentPickupAssignements() {
        return requestRegister.getPickupRegister(pendingRequests);
    }

    /** @return {@link RoboTaxi} curently scheduled to pickup @param request or null if no {@link RoboTaxi}
     *         is scheduled to pickup the {@link PassengerRequest} */
    protected final RoboTaxi getCurrentPickupTaxi(PassengerRequest request) {
        return requestRegister.getPickupRegister(pendingRequests).get(request);
    }

    /** @return divertable RoboTaxis which currently are not on a pickup drive */
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxis() {
        Collection<RoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> !requestRegister.contains(rt)) //
                .collect(Collectors.toList());
        GlobalAssert.that(divertableUnassignedRoboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
        return divertableUnassignedRoboTaxis;
    }

    // **********************************************************************************************
    // ********************* EXTERNAL METHODS TO BE USED BY DISPATCHERS *****************************
    // **********************************************************************************************

    /** Function to assign a vehicle to a request. Only to be used in the redispatch function of shared dispatchers.
     * If another vehicle was assigned to this request this assignment will be aborted and replace with the new assignment
     * 
     * @param roboTaxi
     * @param avRequest */
    public final void addSharedRoboTaxiPickup(RoboTaxi roboTaxi, PassengerRequest avRequest) {
        // System.err.println("ADD " + avRequest.getId() + " TO " + roboTaxi.getId());
        GlobalAssert.that(pendingRequests.contains(avRequest));

        if (requestRegister.contains(avRequest)) {
            if (requestRegister.getAssignedRoboTaxi(avRequest).get() == roboTaxi) {
                // Do nothing as it is already registered for this vehicle
                return;
            }
        }

        // If the request was already assigned remove it from the current vehicle in the request register and update its menu;
        if (requestRegister.contains(avRequest)) {
            // System.err.println("  aborting ...");
            abortAvRequest(avRequest);
        } else {
            periodAssignedRequests.add(avRequest);
        }

        // update the registers
        requestRegister.add(roboTaxi, avRequest);
        roboTaxi.addPassengerRequestToMenu(avRequest);
        GlobalAssert.that(getUniqueRequests(roboTaxi).contains(avRequest));
        reqStatuses.put(avRequest, RequestStatus.ASSIGNED);

        eventsManager.processEvent(new PassengerRequestScheduledEvent(getTimeNow(), mode, avRequest.getId(), avRequest.getPassengerId(), roboTaxi.getId(), 0.0, 0.0));
    }

    protected Set<PassengerRequest> getUniqueRequests(RoboTaxi robotaxi) {
        Set<PassengerRequest> requests = new HashSet<>();

        for (Directive directive : robotaxi.getScheduleManager().getDirectives()) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;
                requests.add(stopDirective.getRequest());
            }
        }

        return requests;
    }

    protected boolean isBusy(RoboTaxi robotaxi) {
        Schedule schedule = robotaxi.getSchedule();
        
        if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED) {
            return true;
        }
        
        Task currentTask = schedule.getCurrentTask();
        
        if (currentTask instanceof DrtDriveTask) {
            OnlineDriveTaskTracker tracker = (OnlineDriveTaskTracker) currentTask.getTaskTracker();
            
            if (tracker.getDiversionPoint() == null) {
                return false;
            }
        }
        
        return pickupTaxis.containsKey(robotaxi) || dropoffTaxis.containsKey(robotaxi);
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
    public final void abortAvRequest(PassengerRequest avRequest) {
        // System.err.println("ABORT " + avRequest.getId());
        GlobalAssert.that(requestRegister.contains(avRequest)); // Only already assigned RoboTaxis are considered else you can not call this function
        // GlobalAssert.that(pendingRequests.contains(avRequest)); // only if a request is not picked up it makes sense to abort it.
        RoboTaxi roboTaxi = requestRegister.getAssignedRoboTaxi(avRequest).orElseThrow(() -> new RuntimeException("This place should not be reached"));
        requestRegister.remove(roboTaxi, avRequest);
        roboTaxi.removePassengerRequestFromMenu(avRequest);
        GlobalAssert.that(Compatibility.of(roboTaxi.getScheduleManager().getDirectives()).forCapacity(roboTaxi.getScheduleManager(), roboTaxi.getCapacity()));
        pendingRequests.add(avRequest);
    }

    /** this function will re-route the taxi if it is not in stay task (for congestion relieving purpose) */
    protected final void reRoute(RoboTaxi roboTaxi) {
        if (!roboTaxi.isInStayTask() && roboTaxi.canReroute())
            timeStepReroute.add(roboTaxi);
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

        // AdaptMenuToDirective.now(roboTaxi, futurePathFactory, now, eventsManager, timeStepReroute.contains(roboTaxi));
        timeStepReroute.clear();
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the
     * fromLink of an {@link PassengerRequest} */
    @Override
    final void executePickups() {
        for (RoboTaxi taxi : getRoboTaxis()) {
            for (Directive directive : taxi.getScheduleManager().getDirectives()) {
                if (directive instanceof StopDirective) {
                    StopDirective stopDirective = (StopDirective) directive;
                    pendingRequests.remove(stopDirective.getRequest());
                }
            }
        }

        Map<PassengerRequest, RoboTaxi> pickupRegisterCopy = new HashMap<>(requestRegister.getPickupRegister(pendingRequests));

        for (RoboTaxi taxi : pickupRegisterCopy.values()) {
            if (pickupTaxis.containsKey(taxi)) {
                continue;
            }

            Task currentTask = taxi.getSchedule().getCurrentTask();

            if (currentTask instanceof AmodeusStopTask) {
                if (((AmodeusStopTask) currentTask).getPickupRequests().size() > 0) {
                    AmodeusStopTask stopTask = (AmodeusStopTask) currentTask;
                    pickupTaxis.put(taxi, stopTask);

                    // System.err.println("Start pickup " + taxi.getId() + " " + printRequests(stopTask.getPickupRequests().values()) + " " + Time.writeTime(getTimeNow()));
                }
            }
        }

        pickupsFromRegisters();
    }

    private void pickupsFromRegisters() {
        Set<RoboTaxi> removeTaxis = new HashSet<>();

        for (Map.Entry<RoboTaxi, AmodeusStopTask> entry : pickupTaxis.entrySet()) {
            AmodeusStopTask task = entry.getValue();

            if (task.getStatus() == TaskStatus.PERFORMED) {
                for (PassengerRequest request : task.getPickupRequests().values()) {
                    reqStatuses.put(request, RequestStatus.DRIVING);
                    periodPickedUpRequests.add(request);
                    ++total_matchedRequests;
                }

                removeTaxis.add(entry.getKey());

                if (task.getPickupRequests().size() > 0) {
                    // System.err.println("Finish pickup " + entry.getKey().getId() + " " + printRequests(task.getPickupRequests().values()) + " " + Time.writeTime(getTimeNow()));
                }
            }
        }

        removeTaxis.forEach(pickupTaxis::remove);
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the toLink of an {@link PassengerRequest} */
    @Override
    final void executeDropoffs() {
        // TODO: This is at an arbitrary location here

        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            roboTaxi.getScheduleManager().updateSequence(getTimeNow());
        }

        /** First the Tasks are assigned. This makes sure the dropoff takes place */
        Map<RoboTaxi, Map<String, PassengerRequest>> requestRegisterCopy = new HashMap<>(requestRegister.getRegister());
        for (RoboTaxi roboTaxi : requestRegisterCopy.keySet()) {
            if (dropoffTaxis.containsKey(roboTaxi)) {
                continue;
            }

            Task currentTask = roboTaxi.getSchedule().getCurrentTask();

            if (currentTask instanceof AmodeusStopTask) {
                if (((AmodeusStopTask) currentTask).getDropoffRequests().size() > 0) {
                    AmodeusStopTask stopTask = (AmodeusStopTask) currentTask;
                    dropoffTaxis.put(roboTaxi, stopTask);

                    // System.err.println("Start dropoff " + roboTaxi.getId() + " " + printRequests(stopTask.getDropoffRequests().values()) + " " + Time.writeTime(getTimeNow()));
                }
            }
        }
        /** Until here only the directives were given. The actual drop off takes place now.
         * From the registers the dropoffs are carried out by the dropoffsFormRegisters() function */
        dropoffsFromRegisters();
    }

    private void dropoffsFromRegisters() {
        /** update all dropoffs which finished the task by now */
        Set<RoboTaxi> removeTaxis = new HashSet<>();

        for (Map.Entry<RoboTaxi, AmodeusStopTask> entry : dropoffTaxis.entrySet()) {
            AmodeusStopTask task = entry.getValue();
            RoboTaxi roboTaxi = entry.getKey();

            if (task.getStatus() == TaskStatus.PERFORMED) {
                for (PassengerRequest request : task.getDropoffRequests().values()) {
                    requestRegister.remove(roboTaxi, request);
                    periodFulfilledRequests.put(request, roboTaxi);
                    reqStatuses.remove(request);
                    total_dropedOffRequests++;
                }

                removeTaxis.add(entry.getKey());

                if (task.getDropoffRequests().size() > 0) {
                    // System.err.println("Finish dropoff " + roboTaxi.getId() + " " + printRequests(task.getDropoffRequests().values()) + " " + Time.writeTime(getTimeNow()));
                }
            }
        }

        removeTaxis.forEach(dropoffTaxis::remove);
    }

    private String printRequests(Collection<PassengerRequest> requests) {
        return "{ " + String.join(", ", requests.stream().map(r -> r.getId().toString()).collect(Collectors.toList())) + " }";
    }

    /** ensures completed redirect tasks are removed from menu */
    @Override
    final void executeRedirects() {
        // getRoboTaxis().forEach(FinishRedirectionIfOnLastLink::now);
    }

    @Override
    /* package */ final void stopAbortedPickupRoboTaxis() {
        // --- Deliberately empty, done in redispatch internal function
    }

    /** called when a new request enters the system, adds request to
     * {@link #pendingRequests}, needs to be public because called from other not
     * derived MATSim functions which are located in another package */
    @Override
    public final void onRequestSubmitted(PassengerRequest request) {
        super.onRequestSubmitted(request);
        reqStatuses.put(request, RequestStatus.REQUESTED);
        periodSubmittdRequests.add(request);
    }

    /** Cleans menu for {@link RoboTaxi} and moves all previously assigned {@link PassengerRequest} back to pending
     * requests taking them out fromrequest- and pickup-registers. */
    /* package */ final void cleanAndAbondon(RoboTaxi roboTaxi) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        Objects.requireNonNull(roboTaxi);

        List<Directive> oldCourses = roboTaxi.cleanAndAbandonMenu();

        for (Directive directive : oldCourses) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;

                pendingRequests.add(stopDirective.getRequest());
                reqStatuses.put(stopDirective.getRequest(), RequestStatus.REQUESTED);
                requestRegister.remove(roboTaxi, stopDirective.getRequest());
            }
        }

        GlobalAssert.that(roboTaxi.getScheduleManager().getDirectives().size() == 0);
        GlobalAssert.that(!requestRegister.contains(roboTaxi));
    }

    /** Consistency checks to be called by
     * {@link RoboTaxiMaintainer#consistencyCheck} in each iteration. */
    @Override
    protected final void consistencySubCheck() {
        // TODO @clruch disable or reduce computational complexity of entire subcheck once API tested for a longer amount of time.

        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            Schedule schedule = roboTaxi.getSchedule();
            Task task = schedule.getCurrentTask();
            /** schedule should never have more than two elements on the next timestep */
            // GlobalAssert.that(MaxTwoMoreTasksAfterEndingOne.check(schedule, task, getTimeNow(), SIMTIMESTEP));

            GlobalAssert.that(roboTaxi.getStatus().equals(SharedRoboTaxiUtils.calculateStatusFromMenu(roboTaxi)));

            if (roboTaxi.getScheduleManager().getDirectives().size() > 0) {
                Directive starter = roboTaxi.getScheduleManager().getDirectives().get(0);

                if (starter instanceof DriveDirective && //
                        roboTaxi.getOnBoardPassengers() == 0) {
                    /** if a redirect meal is next and no customer on board, this is exactly
                     * a rebalcne drive and should be recorded accordingly. */
                    // TODO: Reactivate? GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE));
                }
                
                /** vice versa, if the {@link RoboTaxiStatus} is on REBALANCEDRIVE, it must
                 * be on a redirect task. */
                if (roboTaxi.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE)) {
                    GlobalAssert.that(starter instanceof DriveDirective);
                }
            }
        }

        for (PassengerRequest avRequest : requestRegister.getAssignedAvRequests()) {
            GlobalAssert.that(reqStatuses.containsKey(avRequest));
            if (reqStatuses.get(avRequest).equals(RequestStatus.DRIVING))
                GlobalAssert.that(requestRegister.getAssignedRoboTaxi(avRequest).get().getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
        }

        /** check that each Request only appears once in the Request Register */
        Set<PassengerRequest> uniqueAvRequests = new HashSet<>();
        for (Map<String, PassengerRequest> map : requestRegister.getRegister().values())
            for (PassengerRequest avRequest : map.values()) {
                if (uniqueAvRequests.contains(avRequest))
                    throw new RuntimeException("AV request " + avRequest.getId().toString() + "occurred twice in the request register!");
                uniqueAvRequests.add(avRequest);
            }

        /** there cannot be more pickup than open requests */
        GlobalAssert.that(requestRegister.getAssignedPendingRequests(pendingRequests).size() <= pendingRequests.size());

        /** there cannot be more pickup vehicles than open requests */
        // GlobalAssert.that(getRoboTaxiSubset(RoboTaxiStatus.DRIVETOCUSTOMER).size() <= pendingRequests.size());

        /** all {@link RoboTaxi} in the request Register must have a starter course */
        GlobalAssert.that(requestRegister.getRegister().keySet().stream().allMatch(r -> r.getScheduleManager().getDirectives().size() > 0));

        /** containment check pickupRegisterFunction and pendingRequests */
        GlobalAssert.that(pendingRequests.containsAll(requestRegister.getPickupRegister(pendingRequests).keySet()));

        /** if a request appears in a menu, it must be in the request register */
        for (RoboTaxi roboTaxi : getRoboTaxis())
            if (roboTaxi.getScheduleManager().getDirectives().size() > 0)
                for (Directive directive : roboTaxi.getUnmodifiableViewOfCourses())
                    if (directive instanceof StopDirective) {
                        StopDirective stopDirective = (StopDirective) directive;
                        Map<String, PassengerRequest> requests = requestRegister.get(roboTaxi);
                        
                        if (!requests.containsKey(stopDirective.getRequest().getId().toString())) {
                            System.out.println("ABC");
                        }
                        
                        GlobalAssert.that(requests.containsKey(stopDirective.getRequest().getId().toString()));
                    }

        /** test: every request appears only 2 times, pickup and dropoff across all menus */
        List<String> requestsInMenus = getRoboTaxis().stream().filter(t -> t.getScheduleManager().getDirectives().size() > 0).map(rtx -> //
        getUniqueRequests(rtx)).flatMap(Collection::stream).map(r -> //
        r.getId().toString()).collect(Collectors.toList());
        Set<String> uniqueMenuRequests = new HashSet<>(requestsInMenus);
        GlobalAssert.that(uniqueMenuRequests.size() == requestsInMenus.size());

        /** request register equals the requests in the menu of each {@link RoboTaxi} */
        Set<String> uniqueRegisterRequests = requestRegister.getRegister().values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toSet());
        GlobalAssert.that(uniqueMenuRequests.containsAll(uniqueRegisterRequests));
        GlobalAssert.that(uniqueRegisterRequests.size() == uniqueMenuRequests.size());

        /** on-board customers must equal total_matchedRequests - total_droppedOffRequests , this is computationally
         * very expensive and must be changed eventually . */
        // onboardPassengerCheck.now(total_matchedRequests, total_dropedOffRequests, getRoboTaxis());
    }

    @Override
    /* package */ final void insertRequestInfo(SimulationObjectCompiler simulationObjectCompiler) {
        simulationObjectCompiler.insertRequests(reqStatuses);
        simulationObjectCompiler.insertRequests(periodAssignedRequests, RequestStatus.ASSIGNED);
        simulationObjectCompiler.insertRequests(periodPickedUpRequests, RequestStatus.PICKUP);
        simulationObjectCompiler.insertRequests(periodFulfilledRequests.keySet(), RequestStatus.DROPOFF);
        simulationObjectCompiler.insertRequests(periodSubmittdRequests, RequestStatus.REQUESTED);

        /** insert information of association of {@link RoboTaxi}s and {@link PassengerRequest}s */
        Map<PassengerRequest, RoboTaxi> flatMap = new HashMap<>();
        requestRegister.getRegister().forEach((rt, map) -> map.values().forEach(avr -> flatMap.put(avr, rt)));
        // adds the robotaxi for dropped off requests (not in requestRegister anymore)
        periodFulfilledRequests.forEach(flatMap::put);

        simulationObjectCompiler.addRequestRoboTaxiAssoc(flatMap);

        /** clear all the request Registers */
        periodAssignedRequests.clear();
        periodPickedUpRequests.clear();
        periodFulfilledRequests.clear();
        periodSubmittdRequests.clear();
    }

    /** adding a vehicle during setup of simulation, handled by {@link AmodeusGenerator} */
    @Override
    public final void addVehicle(DvrpVehicle vehicle) {
        super.addVehicle(vehicle, RoboTaxiUsageType.SHARED);
    }

    @Override
    protected final void updateDivertableLocations() {
        // Check that we really use the right SIMTime Step.
        // its done here as this function is calle before the step
        if (Objects.nonNull(lastTime))
            GlobalAssert.that(SIMTIMESTEP == getTimeNow() - lastTime); // Make sure the hard coded Time step is chosen right
        lastTime = getTimeNow();

        // Update the divertable Location
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            Schedule schedule = roboTaxi.getSchedule();
            new RoboTaxiTaskAdapter(schedule.getCurrentTask()) {
                @Override
                public void handle(DrtDriveTask avDriveTask) {
                    OnlineDriveTaskTracker taskTracker = (OnlineDriveTaskTracker) avDriveTask.getTaskTracker();
                    LinkTimePair linkTimePair = Objects.requireNonNull(taskTracker.getDiversionPoint());
                    roboTaxi.setDivertableLinkTime(linkTimePair); // contains null check
                    roboTaxi.setCurrentDriveDestination(avDriveTask.getPath().getToLink());
                }

                @Override
                public void handle(AmodeusStopTask avStopTask) {
                    // GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                }

                @Override
                public void handle(DrtStayTask avStayTask) {
                    // for empty vehicles the current task has to be the last task
                    if (ScheduleUtils.isLastTask(schedule, avStayTask) && !requestRegister.contains(roboTaxi) && !periodFulfilledRequests.containsValue(roboTaxi)) {
                        GlobalAssert.that(avStayTask.getBeginTime() <= getTimeNow());
                        GlobalAssert.that(Objects.nonNull(avStayTask.getLink()));
                        roboTaxi.setDivertableLinkTime(new LinkTimePair(avStayTask.getLink(), getTimeNow()));
                        roboTaxi.setCurrentDriveDestination(avStayTask.getLink());
                        if (roboTaxi.getScheduleManager().getDirectives().size() == 0)
                            GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
                    }
                }
            };
        }
    }
}
