/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.events.Event;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** This class contains all functionality which is used by both unit capacity
 * dispatchers and shared {@link RoboTaxi} dispatchers. */
/* package */ abstract class BasicUniversalDispatcher extends RoboTaxiMaintainer {

    /* package */ final Set<AVRequest> pendingRequests = new LinkedHashSet<>();
    protected int publishPeriod; // not final, so that dispatchers can disable, or manipulate

    /* package */ final MatsimAmodeusDatabase db;
    /* package */ final FuturePathFactory futurePathFactory;
    /* package */ final double pickupDurationPerStop;
    /* package */ final double dropoffDurationPerStop;

    /* package */ int total_matchedRequests = 0;

    public BasicUniversalDispatcher(EventsManager eventsManager, Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            MatsimAmodeusDatabase db) {
        super(eventsManager, config, avDispatcherConfig);
        this.db = db;
        futurePathFactory = new FuturePathFactory(parallelLeastCostPathCalculator, travelTime);
        pickupDurationPerStop = avDispatcherConfig.getParent().getTimingParameters().getPickupDurationPerStop();
        dropoffDurationPerStop = avDispatcherConfig.getParent().getTimingParameters().getDropoffDurationPerStop();
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        publishPeriod = safeConfig.getInteger("publishPeriod", 10);
    }

    /** @return {@Collection} of all {@AVRequests} which are currently open.
     *         Requests are removed from list in setAcceptRequest function. */
    protected synchronized final Collection<AVRequest> getAVRequests() {
        return Collections.unmodifiableCollection(pendingRequests);
    }

    /** @return {@link List} of all {@link RoboTaxi}s which are in
     *         {@link RoboTaxiStatus} @param status, sample usage:
     *         getRoboTaxiSubset(AVStatus.STAY, AVStatus.DRIVEWITHCUSTOMER) */
    protected final List<RoboTaxi> getRoboTaxiSubset(RoboTaxiStatus... status) {
        return getRoboTaxiSubset(EnumSet.copyOf(Arrays.asList(status)));
    }

    protected List<RoboTaxi> getRoboTaxiSubset(Set<RoboTaxiStatus> status) {
        return getRoboTaxis().stream().filter(rt -> status.contains(rt.getStatus())).collect(Collectors.toList());
    }

    /** @return {@Collection} of {@RoboTaxi}s which can be redirected during
     *         iteration, for a shared {@link RoboTaxi}, any vehicle can be diverted unless
     *         it has a directive in the current ime step or it is on the last link of its
     *         directive. */
    protected final Collection<RoboTaxi> getDivertableRoboTaxis() {
        return getRoboTaxis().stream() //
                .filter(RoboTaxi::isDivertable) //
                .collect(Collectors.toList());
    }

    /** Adding a @param vehicle during setup of simulation handled by {@link AVGenerator},
     * the parameter @param singleOrShared indicates if multi-passenger ride-sharing case
     * or unit capacity case. */
    protected void addVehicle(AVVehicle vehicle, RoboTaxiUsageType singleOrShared) {
        RoboTaxi roboTaxi = new RoboTaxi(vehicle, new LinkTimePair(vehicle.getStartLink(), 0.0), vehicle.getStartLink(), singleOrShared);
        Event event = new AVVehicleAssignmentEvent(vehicle, 0);
        addRoboTaxi(roboTaxi, event);
    }

    /** called when a new request enters the system, adds request to
     * {@link pendingRequests}, needs to be public because called from other not
     * derived MATSim functions which are located in another package */
    @Override
    public void onRequestSubmitted(AVRequest request) {
        boolean added = pendingRequests.add(request);
        GlobalAssert.that(added);
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