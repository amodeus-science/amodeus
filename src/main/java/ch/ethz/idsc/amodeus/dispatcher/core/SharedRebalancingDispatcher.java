/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** abstract base class for dispatchers that perform relocation of empty
 * vehicles (rebalancing) or redirect vehicles during customer journeys
 * to links which are not a dropoff or pickup location */
public abstract class SharedRebalancingDispatcher extends SharedUniversalDispatcher {

    protected SharedRebalancingDispatcher(Config config, OperatorConfig operatorConfig, TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator,
            EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, parallelLeastCostPathCalculator, eventsManager, db);
    }

    /** @param roboTaxi is rebalanced to
     * @param destination and all the oustanding pickup and dropoff tasks are deleted */
    protected final void setRoboTaxiRebalance(RoboTaxi roboTaxi, final Link destination) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        /** clear menu and put requests back to pending requests */
        cleanAndAbondon(roboTaxi);
        GlobalAssert.that(!SharedCourseAccess.hasStarter(roboTaxi));
        SharedCourse redirectCourse = SharedCourse.redirectCourse(destination, Double.toString(getTimeNow()) + roboTaxi.getId().toString());
        addSharedRoboTaxiRedirect(roboTaxi, redirectCourse);
    }

    /** {@link RoboTaxi} @param roboTaxi is redirected to the {@link Link} of the {@link SharedCourse}
     * the course can be moved to another position in the {@link SharedMenu} of the {@link} RoboTaxi */
    protected static void addSharedRoboTaxiRedirect(RoboTaxi roboTaxi, SharedCourse redirectCourse) {
        GlobalAssert.that(redirectCourse.getMealType().equals(SharedMealType.REDIRECT));
        roboTaxi.addRedirectCourseToMenu(redirectCourse);
    }

    /** @return {@link List } of all {@link RoboTaxi} which are currently rebalancing. */
    protected List<RoboTaxi> getRebalancingRoboTaxis() {
        return getRoboTaxis().stream()//
                .filter(rt -> rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE))//
                .collect(Collectors.toList());
    }

    /** @return {@link List} of all {@link RoboTaxi} which are divertable and not in a rebalacing task. */
    protected List<RoboTaxi> getDivertableNotRebalancingRoboTaxis() {
        return getDivertableRoboTaxis().stream()//
                .filter(rt -> !rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE))//
                .collect(Collectors.toList());
    }
}
