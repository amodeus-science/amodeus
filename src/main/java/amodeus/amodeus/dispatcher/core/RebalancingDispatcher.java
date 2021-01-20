/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.List;
import java.util.stream.Collectors;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.drt.relocation.RelocationScheduledEvent;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.common.collect.ImmutableList;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.DriveDirective;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.util.math.GlobalAssert;

/** abstract base class for dispatchers that perform relocation of empty
 * vehicles (rebalancing) or redirect vehicles during customer journeys
 * to links which are not a dropoff or pickup location */
public abstract class RebalancingDispatcher extends UniversalDispatcher {

    protected RebalancingDispatcher(Config config, AmodeusModeConfig operatorConfig, TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator,
            EventsManager eventsManager, //
            MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy, RoboTaxiUsageType usageType) {
        super(config, operatorConfig, travelTime, parallelLeastCostPathCalculator, eventsManager, db, rebalancingStrategy, usageType);
    }

    /** @param roboTaxi is rebalanced to
     * @param destination and all the oustanding pickup and dropoff tasks are deleted */
    protected final void setRoboTaxiRebalance(RoboTaxi roboTaxi, final Link destination) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());

        if (!isRebalancingTo(roboTaxi, destination)) {
            /** clear menu and put requests back to pending requests */
            cleanAndAbondon(roboTaxi);
            GlobalAssert.that(roboTaxi.getScheduleManager().getDirectives().size() == 0);
            Directive directive = Directive.drive(destination);

            Link originLink = roboTaxi.getDivertableLocation();
            Link destinationLink = Directive.getLink(directive);

            eventsManager.processEvent(
                    new RelocationScheduledEvent(getTimeNow(), mode, roboTaxi.getId(), roboTaxi.getDivertableLocation().getId(), originLink.getId(), destinationLink.getId()));

            roboTaxi.addRedirectCourseToMenu((DriveDirective) directive);
        }

        if (usageType.equals(RoboTaxiUsageType.SINGLEUSED)) {
            roboTaxi.lock();
        }
    }

    /** {@link RoboTaxi} @param roboTaxi is redirected to the {@link Link} of the {@link SharedCourse}
     * the course can be moved to another position in the {@link SharedMenu} of the {@link} RoboTaxi */
    protected void addSharedRoboTaxiRedirect(RoboTaxi roboTaxi, Directive directive) {
        GlobalAssert.that(directive instanceof DriveDirective);
        boolean isReblancingToDestination = isRebalancingTo(roboTaxi, Directive.getLink(directive));

        if (!isReblancingToDestination) {
            ImmutableList<Directive> directives = roboTaxi.getScheduleManager().getDirectives();
            Link originLink = directives.size() > 0 ? Directive.getLink(directives.get(directives.size() - 1)) : roboTaxi.getDivertableLocation();
            Link destinationLink = Directive.getLink(directive);

            eventsManager.processEvent(
                    new RelocationScheduledEvent(getTimeNow(), mode, roboTaxi.getId(), roboTaxi.getDivertableLocation().getId(), originLink.getId(), destinationLink.getId()));

            roboTaxi.addRedirectCourseToMenu((DriveDirective) directive);
        }

        if (usageType.equals(RoboTaxiUsageType.SINGLEUSED)) {
            roboTaxi.lock();
        }
    }

    private boolean isRebalancingTo(RoboTaxi vehicle, Link destinationLink) {
        List<Directive> directives = vehicle.getScheduleManager().getDirectives();

        if (directives.size() > 0) {
            Directive lastDirective = directives.get(directives.size() - 1);

            if (lastDirective instanceof DriveDirective) {
                DriveDirective driveDirective = (DriveDirective) lastDirective;

                if (driveDirective.getDestination().equals(destinationLink)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** @return {@link List } of all {@link RoboTaxi} which are currently rebalancing. */
    protected final List<RoboTaxi> getRebalancingRoboTaxis() {
        return getRoboTaxis().stream() //
                .filter(rt -> rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE)) //
                .collect(Collectors.toList());
    }

    /** @return {@link List} of all {@link RoboTaxi} which are divertable and not in a rebalacing task. */
    protected final List<RoboTaxi> getDivertableNotRebalancingRoboTaxis() {
        return getDivertableRoboTaxis().stream() //
                .filter(rt -> !rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE)) //
                .collect(Collectors.toList());
    }
}
