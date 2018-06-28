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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Vehicle;
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

/**
 * purpose of {@link UniversalDispatcher} is to collect and manage
 * {@link AVRequest}s alternative implementation of {@link AVDispatcher};
 * supersedes {@link AbstractDispatcher}.
 */
/*
 * added new requestRegister which does hold information from request to dropoff
 * of each request which is written to the simulationObject implementation is
 * not very clean yet but functionally stable, depending on the publishPeriod,
 * andya jan '18
 */
public abstract class SharedUniversalDispatcher extends SharedRoboTaxiMaintainer {

	private final FuturePathFactory futurePathFactory;
	private final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
	private final Map<AVRequest, SharedRoboTaxi> pickupRegister = new HashMap<>(); // new RequestRegister
	private final Map<SharedRoboTaxi, Map<Id<Request>, AVRequest>> requestRegister = new HashMap<>();
	// protected final Map<SharedRoboTaxi, SharedAVMenu> sharedAvMenus = new
	// HashMap<>();
	private final Map<AVRequest, SharedRoboTaxi> periodFulfilledRequests = new HashMap<>(); // new
																							// temporaryRequestRegister
																							// for fulfilled requests
	private final Map<Id<Vehicle>, RoboTaxiStatus> oldRoboTaxis = new HashMap<>();
	private final double pickupDurationPerStop;
	private final double dropoffDurationPerStop;
	protected int publishPeriod; // not final, so that dispatchers can disable, or manipulate
	private int total_matchedRequests = 0;

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

	/**
	 * @return {@Collection} of all {@AVRequests} which are currently open. Requests
	 *         are removed from list in setAcceptRequest function.
	 */
	protected synchronized final Collection<AVRequest> getAVRequests() {
		return Collections.unmodifiableCollection(pendingRequests);
	}

	/** @return AVRequests which are currently not assigned to a vehicle */
	protected synchronized final List<AVRequest> getUnassignedAVRequests() {
		return pendingRequests.stream() //
				.filter(r -> !pickupRegister.containsKey(r)) //
				.collect(Collectors.toList());
	}

	/**
	 * Example call: getRoboTaxiSubset(AVStatus.STAY, AVStatus.DRIVEWITHCUSTOMER)
	 * 
	 * @param status
	 *            {@AVStatus} of desiredsRoboTaxis, e.g., STAY,DRIVETOCUSTOMER,...
	 * @return list ofsRoboTaxis which are in {@AVStatus} status
	 */
	public final List<SharedRoboTaxi> getRoboTaxiSubset(RoboTaxiStatus... status) {
		return getRoboTaxiSubset(EnumSet.copyOf(Arrays.asList(status)));
	}

	private List<SharedRoboTaxi> getRoboTaxiSubset(Set<RoboTaxiStatus> status) {
		return getRoboTaxis().stream().filter(rt -> status.contains(rt.getStatus())).collect(Collectors.toList());
	}

	/** @return divertablesRoboTaxis which currently not on a pickup drive */
	protected final Collection<SharedRoboTaxi> getDivertableUnassignedRoboTaxis() {
		Collection<SharedRoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
				.filter(rt -> !pickupRegister.containsValue(rt)) //
				.collect(Collectors.toList());
		GlobalAssert.that(!divertableUnassignedRoboTaxis.stream().anyMatch(pickupRegister::containsValue));
		GlobalAssert.that(divertableUnassignedRoboTaxis.stream().allMatch(SharedRoboTaxi::isWithoutCustomer));
		return divertableUnassignedRoboTaxis;
	}

	/**
	 * @return {@Collection} of {@RoboTaxi} which can be redirected during iteration
	 */
	protected final Collection<SharedRoboTaxi> getDivertableRoboTaxis() {
		return getRoboTaxis().stream() //
				.filter(SharedRoboTaxi::isWithoutDirective) //
				.filter(SharedRoboTaxi::isWithoutCustomer) //
				.filter(SharedRoboTaxi::notDrivingOnLastLink) //
				.collect(Collectors.toList());
	}

	/**
	 * @return immutable and inverted copy of pickupRegister, displays which
	 *         vehicles are currently scheduled to pickup which request
	 */
	// protected final Map<SharedRoboTaxi, List<AVRequest>> getPickupRoboTaxis() {
	// Map<SharedRoboTaxi, List<AVRequest>> pickupPairs =
	// pickupRegister.entrySet().stream()//
	// .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	// GlobalAssert.that(pickupPairs.keySet().stream().allMatch(rt ->
	// rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER)));
	// return pickupPairs;
	// }

	public void addSharedRoboTaxiPickup(SharedRoboTaxi sRoboTaxi, AVRequest avRequest) {
		GlobalAssert.that(sRoboTaxi.canPickupNewCustomer());
		GlobalAssert.that(pendingRequests.contains(avRequest));

		if (!requestRegister.containsKey(sRoboTaxi)) {
			requestRegister.put(sRoboTaxi, new HashMap<>());
		}

		// delete from request register if the request was previousely in the pickup
		// register
		if (pickupRegister.containsKey(avRequest))
			requestRegister.get(pickupRegister.get(avRequest)).remove(avRequest.getId());
		// 1) enter information into pickup table
		pickupRegister.put(avRequest, sRoboTaxi);
		// 2) also do everything for the full-time requestRegister
		requestRegister.get(sRoboTaxi).put(avRequest.getId(), avRequest);

		sRoboTaxi.getMenu().addAVCourseAsaDessert(new SharedAVCourse(avRequest.getId(), SharedAVMealType.PICKUP));
		sRoboTaxi.getMenu().addAVCourseAsaDessert(new SharedAVCourse(avRequest.getId(), SharedAVMealType.DROPOFF));

	}

	@Override
	final void redispatchInternal(double now) {

		Map<SharedRoboTaxi, SharedAVMenu> sharedAvMenuLastStep = new HashMap<>();
		getRoboTaxis().forEach(rt -> sharedAvMenuLastStep.put(rt, rt.getMenu().copy()));

		// To be implemented externally in the dispatchers
		redispatch(now);

		for (SharedRoboTaxi sharedRoboTaxi : getRoboTaxis()) {
			SharedAVMenu menu = sharedRoboTaxi.getMenu();
			GlobalAssert.that(menu.checkNoPickupAfterDropoffOfSameRequest());

			if (!menu.equals(sharedAvMenuLastStep.get(sharedRoboTaxi))) {
				RoboTaxiStatus avStatus = null;
				Link destLink = getStarterLink(sharedRoboTaxi);
				if (menu.getSharedAVStarter().getPickupOrDropOff().equals(SharedAVMealType.PICKUP)) {
					avStatus = sharedRoboTaxi.getCurrentNumberOfCustomersOnBoard() > 0
							? RoboTaxiStatus.DRIVEWITHCUSTOMER
							: RoboTaxiStatus.DRIVETOCUSTOMER;
					GlobalAssert.that(sharedRoboTaxi.canPickupNewCustomer()); // TODO Check

				} else {
					avStatus = RoboTaxiStatus.DRIVEWITHCUSTOMER;
				}
				setRoboTaxiDiversion(sharedRoboTaxi, destLink, avStatus);

			}
		}

	}

	// private void setRoboTaxiForMeal(SharedRoboTaxi sRoboTaxi) {
	//
	// SharedAVCourse course = sharedAvMenus.get(sRoboTaxi).getSharedAVStarter();
	// AVRequest avR = requestRegister.get(sRoboTaxi).get(course.getRequestId());
	//
	// if (course.getPickupOrDropOff().equals(SharedAVMealType.PICKUP)) {
	// RoboTaxiStatus status = sRoboTaxi.getCurrentNumberOfCustomersOnBoard() > 0
	// ? RoboTaxiStatus.DRIVEWITHCUSTOMER
	// : RoboTaxiStatus.DRIVETOCUSTOMER;
	// GlobalAssert.that(sRoboTaxi.canPickupNewCustomer()); // TODO Check
	// setRoboTaxiDiversion(sRoboTaxi, avR.getFromLink(), status);
	// } else {
	// setRoboTaxiDiversion(sRoboTaxi, avR.getToLink(),
	// RoboTaxiStatus.DRIVEWITHCUSTOMER);
	// }
	//
	// }

	private Link getStarterLink(SharedRoboTaxi sRoboTaxi) {
		SharedAVCourse course = sRoboTaxi.getMenu().getSharedAVStarter();

		AVRequest avR = requestRegister.get(sRoboTaxi).get(course.getRequestId());

		if (course.getPickupOrDropOff().equals(SharedAVMealType.PICKUP)) {
			return avR.getFromLink();
		} else if (course.getPickupOrDropOff().equals(SharedAVMealType.DROPOFF)) {
			return avR.getToLink();
		} else {
			throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
		}

	}

	// ===================================================================================
	// INTERNAL Methods, do not call from derived dispatchers.

	/**
	 * For UniversalDispatcher, VehicleMaintainer internal use only. Use
	 * {@link UniveralDispatcher.setRoboTaxiPickup} or {@link setRoboTaxiRebalance}
	 * from dispatchers. Assigns new destination to vehicle, if vehicle is already
	 * located at destination, nothing happens. In one pass of {@redispatch(...)} in
	 * {@VehicleMaintainer}, the function setVehicleDiversion(...) may only be
	 * invoked once for a single {@RoboTaxi} vehicle
	 *
	 * @paramsRoboTaxi {@link SharedRoboTaxi} supplied with a getFunction,e.g.,
	 *                 {@link this.getDivertableRoboTaxis}
	 * @param destination
	 *            {@link Link} the {@link SharedRoboTaxi} should be diverted to
	 * @param avstatus
	 *            {@link} the {@link AVStatus} the {@link SharedRoboTaxi} has after
	 *            the diversion, depends if used from {@link setRoboTaxiPickup} or
	 *            {@link setRoboTaxiRebalance}
	 */
	final void setRoboTaxiDiversion(SharedRoboTaxi sRoboTaxi, Link destination, RoboTaxiStatus avstatus) {
		// updated status ofsRoboTaxi
		// GlobalAssert.that(sRoboTaxi.isWithoutDirective()); // FIXME

		sRoboTaxi.setStatus(avstatus);

		// udpate schedule ofsRoboTaxi
		final Schedule schedule = sRoboTaxi.getSchedule();
		Task task = schedule.getCurrentTask(); // <- implies that task is started
		new RoboTaxiTaskAdapter(task) {

			@Override
			public void handle(AVDriveTask avDriveTask) {
				if (!avDriveTask.getPath().getToLink().equals(destination)) { // ignore when vehicle is already going
					FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
							sRoboTaxi.getDivertableLocation(), destination, sRoboTaxi.getDivertableTime());

					sRoboTaxi.assignDirective(new SharedGeneralDriveDiversionDirective(sRoboTaxi, destination,
							futurePathContainer, getTimeNow()));

				} else
					sRoboTaxi.assignDirective(EmptyDirective.INSTANCE);
			}

			@Override
			public void handle(AVStayTask avStayTask) {
				if (!avStayTask.getLink().equals(destination)) { // ignore request where location == target
					FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
							sRoboTaxi.getDivertableLocation(), destination, sRoboTaxi.getDivertableTime());

					sRoboTaxi.assignDirective(
							new SharedGeneralStayDirective(sRoboTaxi, destination, futurePathContainer, getTimeNow()));

				} else
					sRoboTaxi.assignDirective(EmptyDirective.INSTANCE);
			}

			// FIXME
			@Override
			public void handle(AVPickupTask avPickupTask) {
				handlePickupAndDropoff(sRoboTaxi, task);
			}

			@Override
			public void handle(AVDropoffTask dropOffTask) {
				handlePickupAndDropoff(sRoboTaxi, task);
			}

			private void handlePickupAndDropoff(SharedRoboTaxi sRoboTaxi, Task task) {
				Link nextLink = getStarterLink(sRoboTaxi); // We are already at next course in the menu although in
				// matsim the pickup is still happening
				// TODO Check
				GlobalAssert.that(nextLink.equals(destination));
				FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
						sRoboTaxi.getDivertableLocation(), nextLink, task.getEndTime());

				sRoboTaxi.assignDirective(new SharedGeneralPickupOrDropoffDiversionDirective(sRoboTaxi,
						futurePathContainer, getTimeNow()));
			}
		};
	}

	/**
	 * complete all matchings if a {@link SharedRoboTaxi} has arrived at the
	 * fromLink of an {@link AVRequest}
	 */
	@Override
	void executePickups() {
		Map<AVRequest, SharedRoboTaxi> pickupRegisterCopy = new HashMap<>(pickupRegister);
		// TODO Ian is there any way of getting unique map values in a more efficient
		// way
		Set<SharedRoboTaxi> uniqueRt = new HashSet<>();
		pickupRegisterCopy.values().stream().forEach(rt -> uniqueRt.add(rt));
		for (SharedRoboTaxi sRt : uniqueRt) {
			// FIXME Is the divertable Location really the current position of the vehicle????
			Link pickupVehicleLink = sRt.getDivertableLocation();
			// TODO note that waiting for last staytask adds a one second staytask before
			// switching to pickuptask
			// TODO why exactely one second? couldnt it be up to 9s? (lukas)
			boolean isOk = sRt.getSchedule().getCurrentTask() == Schedules.getLastTask(sRt.getSchedule()); // instanceof

			SharedAVCourse currentCourse = sRt.getMenu().getSharedAVStarter();
			AVRequest avR = requestRegister.get(sRt).get(currentCourse.getRequestId());

			GlobalAssert.that(pendingRequests.contains(avR));
			GlobalAssert.that(pickupRegisterCopy.containsKey(avR));

			if (currentCourse.getPickupOrDropOff().equals(SharedAVMealType.PICKUP)
					&& avR.getFromLink().equals(pickupVehicleLink) && isOk) {
				setAcceptRequest(sRt, avR);
			}
		}
	}
	
	/**
	 * Function called from {@link UniversalDispatcher.executePickups} if asRoboTaxi
	 * scheduled for pickup has reached the from link of the {@link AVRequest}.
	 * 
	 * @paramsRoboTaxi
	 * @param avRequest
	 */
	private synchronized final void setAcceptRequest(SharedRoboTaxi sRoboTaxi, AVRequest avRequest) {
		Link pickupLink = getStarterLink(sRoboTaxi);

		GlobalAssert.that(avRequest.getFromLink().equals(pickupLink));
		// FIXME make a check that the position of the robotaxi mathces the request location. dependent on the location chosen in exxecute pickups
//		GlobalAssert.that(avRequest.getFromLink().equals(sRoboTaxi.getDivertableLocation()));

		sRoboTaxi.setStatus(RoboTaxiStatus.DRIVEWITHCUSTOMER);
		sRoboTaxi.setCurrentDriveDestination(avRequest.getFromLink()); // TODO toLink
		{
			boolean statusPen = pendingRequests.remove(avRequest);
			GlobalAssert.that(statusPen);
		}
		{
			SharedRoboTaxi former = pickupRegister.remove(avRequest);
			GlobalAssert.that(sRoboTaxi == former);
		}

		consistencySubCheck();

		final Schedule schedule = sRoboTaxi.getSchedule();
		// check that current task is last task in schedule
		// TODO fix
		GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // instanceof AVDriveTask);

		final double endPickupTime = getTimeNow() + pickupDurationPerStop;

		sRoboTaxi.pickupNewCustomerOnBoard();
		// sRoboTaxi.pickupCustomer(avRequest.getId());

		// Remove pickup from menu
		// sRoboTaxi.getMenu().removeAVCourse(0);

		FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getFromLink(),
				getStarterLink(sRoboTaxi), endPickupTime);
		sRoboTaxi.assignDirective(
				new SharedGeneralDriveDirectivePickup(sRoboTaxi, avRequest, futurePathContainer, getTimeNow()));

		// FIXME
		final double distance = VrpPathUtils.getDistance(futurePathContainer.getVrpPathWithTravelData());
		avRequest.getRoute().setDistance(distance);

		++total_matchedRequests;
	}

	/**
	 * Function called from {@link UniversalDispatcher.executeDropoffs} if
	 * asRoboTaxi scheduled for dropoff has reached the from link of the
	 * {@link AVRequest}.
	 * 
	 * @paramsRoboTaxi
	 * @param avRequest
	 */
	private synchronized final void setPassengerDropoff(SharedRoboTaxi sRoboTaxi, AVRequest avRequest) {
		GlobalAssert.that(requestRegister.get(sRoboTaxi).containsValue(avRequest));

		requestRegister.get(sRoboTaxi).remove(avRequest.getId());

		// save avRequests which are matched for one publishPeriod to ensure no requests
		// are lost in the recording.
		periodFulfilledRequests.put(avRequest, sRoboTaxi);

		final Schedule schedule = sRoboTaxi.getSchedule();
		// check that current task is last task in schedule
		GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // instanceof AVDriveTask);

		final double endDropOffTime = getTimeNow() + dropoffDurationPerStop;

		sRoboTaxi.dropOffCustomer();

		SharedAVCourse nextCourse = sRoboTaxi.getMenu().getSharedAVStarter();
		FuturePathContainer futurePathContainer = (nextCourse != null)
				? futurePathFactory.createFuturePathContainer(avRequest.getToLink(), getStarterLink(sRoboTaxi),
						endDropOffTime)
				: futurePathFactory.createFuturePathContainer(avRequest.getToLink(), avRequest.getToLink(),
						endDropOffTime);
		sRoboTaxi.assignDirective(new SharedGeneralDriveDirectiveDropoff(sRoboTaxi, avRequest, futurePathContainer,
				getTimeNow(), dropoffDurationPerStop));

		// FIXME temporary fix.. else it fails in execute dropoffs since there are
		// Seems as a good alternative as we check each time if the robotaxi exists in the map... (lukas)
		// robotaxis in map but with no requests assigned.
		if (nextCourse == null)
			requestRegister.remove(sRoboTaxi);
	}

	@Override
	/* package */ final boolean isInPickupRegister(SharedRoboTaxi sRoboTaxi) {
		return pickupRegister.containsValue(sRoboTaxi);
	}

	@Override
	/* package */ final boolean isInRequestRegister(SharedRoboTaxi sRoboTaxi) {
		return requestRegister.containsKey(sRoboTaxi);
	}

	/**
	 * @param avRequest
	 * @returnsRoboTaxi assigned to given avRequest, or empty if no taxi is assigned
	 *                  to avRequest Used by BipartiteMatching in
	 *                  euclideanNonCyclic, there a comparison to the old av
	 *                  assignment is needed
	 */
	public final Optional<SharedRoboTaxi> getPickupTaxi(AVRequest avRequest) {
		return Optional.ofNullable(pickupRegister.get(avRequest));
	}



	/**
	 * complete all matchings if a {@link SharedRoboTaxi} has arrived at the toLink
	 * of an {@link AVRequest}
	 */
	@Override
	void executeDropoffs() {
		Map<SharedRoboTaxi, Map<Id<Request>, AVRequest>> requestRegisterCopy = new HashMap<>(requestRegister);
		for (SharedRoboTaxi dropoffVehicle : requestRegisterCopy.keySet()) {
			Link dropoffVehicleLink = dropoffVehicle.getDivertableLocation();
			// TODO note that waiting for last staytask adds a one second staytask before
			// switching to dropoffTask
			boolean isOk = dropoffVehicle.getSchedule().getCurrentTask() == Schedules
					.getLastTask(dropoffVehicle.getSchedule()); // instanceof AVDriveTask;

			SharedAVCourse currentCourse = dropoffVehicle.getMenu().getSharedAVStarter();
			AVRequest avR = requestRegister.get(dropoffVehicle).get(currentCourse.getRequestId());

			if (currentCourse.getPickupOrDropOff().equals(SharedAVMealType.DROPOFF)
					&& avR.getToLink().equals(dropoffVehicleLink) && isOk) {
				setPassengerDropoff(dropoffVehicle, avR);
			}
		}
	}

	/**
	 * called when a new request enters the system, adds request to
	 * {@link pendingRequests}, needs to be public because called from other not
	 * derived MATSim functions which are located in another package
	 */
	@Override
	public final void onRequestSubmitted(AVRequest request) {
		boolean added = pendingRequests.add(request); // <- store request
		GlobalAssert.that(added);
	}

	/**
	 * function stops {@link SharedRoboTaxi} which are still heading towards an
	 * {@link AVRequest} but another {@link SharedRoboTaxi} was scheduled to pickup
	 * this {@link AVRequest} in the meantime
	 */
	@Override
	/* package */ final void stopAbortedPickupRoboTaxis() {

		// stop vehicles still driving to a request but other taxi serving that request
		// already
		getRoboTaxis().stream()//
				.filter(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER))//
				.filter(rt -> !pickupRegister.containsValue(rt))//
				.filter(SharedRoboTaxi::canPickupNewCustomer)//
				.filter(SharedRoboTaxi::isWithoutDirective)//
				.forEach(rt -> setRoboTaxiDiversion(rt, rt.getDivertableLocation(), RoboTaxiStatus.REBALANCEDRIVE));
		GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());
	}

	/**
	 * Consistency checks to be called by
	 * {@linksRoboTaxiMaintainer.consistencyCheck} in each iteration.
	 */
	@Override
	protected final void consistencySubCheck() {
		// TODO checked
		// there cannot be more pickup vehicles than open requests
		GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());

		// pickupRegister needs to be a subset of requestRegister
		pickupRegister.forEach((k, v) -> GlobalAssert.that(requestRegister.get(v).containsValue(k)));

		// containment check pickupRegister and pendingRequests
		pickupRegister.keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

		// ensure no RoboTaxi is scheduled to pickup two requests
		// GlobalAssert.that(pickupRegister.size() ==
		// pickupRegister.values().stream().distinct().count());

		// int numberCustomersOnBoard = getRoboTaxis().stream().mapToInt(sRt ->
		// sRt.getCurrentNumberOfCustomersOnBoard()).sum();

	}

	/**
	 * save simulation data into {@link SimulationObject} for later analysis and
	 * visualization.
	 */
	@Override
	protected final void notifySimulationSubscribers(long round_now, StorageUtils storageUtils) {
		// Save a simulation object each publish Period
		if (publishPeriod > 0 && round_now % publishPeriod == 0) {
			
			SharedSimulationObjectCompiler simulationObjectCompiler = SharedSimulationObjectCompiler.create( //
					round_now, getInfoLine(), total_matchedRequests);

			// FIXME CREATE NEW LOGIC FOR SIMULATION OBJECTS!
//			Map<AVRequest, SharedRoboTaxi> newRegister = requestRegister;
			List<SharedRoboTaxi> newRoboTaxis = getRoboTaxis();

			simulationObjectCompiler.insertFulfilledRequests(periodFulfilledRequests.keySet());
			simulationObjectCompiler.insertRequests(requestRegister, oldRoboTaxis);

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
