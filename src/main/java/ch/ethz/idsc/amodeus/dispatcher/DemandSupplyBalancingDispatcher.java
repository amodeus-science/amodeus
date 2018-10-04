/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfig;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMaintainer;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** Implementation of the "demand-supply-balancing" dispatching heuristic presented in
 * Maciejewski, Michal, and Joschka Bischoff. "Large-scale microscopic simulation of taxi services."
 * Procedia Computer Science 52 (2015): 358-364. */
public class DemandSupplyBalancingDispatcher extends UniversalDispatcher {

    private final int dispatchPeriod;
    /** data structures are used to enable fast "contains" searching */
    private final TreeMaintainer<AVRequest> requestMaintainer;
    private final TreeMaintainer<RoboTaxi> unassignedRoboTaxis;

    private DemandSupplyBalancingDispatcher(Config config, AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, Network network, //
            MatsimAmodeusDatabase db) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, db);
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(10);
        this.requestMaintainer = new TreeMaintainer<>(network, this::getLocation);
        this.unassignedRoboTaxis = new TreeMaintainer<>(network, this::getRoboTaxiLoc);
    }

    @Override
    public void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            /** get open requests and available vehicles */
            Collection<RoboTaxi> robotaxisDivertable = getDivertableUnassignedRoboTaxis();
            getRoboTaxiSubset(RoboTaxiStatus.STAY).stream().forEach(rt -> unassignedRoboTaxis.add(rt));
            List<AVRequest> requests = getUnassignedAVRequests();
            requests.stream().forEach(r -> requestMaintainer.add(r));

            /** distinguish over- and undersupply cases */
            boolean oversupply = false;
            if (unassignedRoboTaxis.size() >= requests.size())
                oversupply = true;

            if (unassignedRoboTaxis.size() > 0 && requests.size() > 0) {
                /** oversupply case */
                if (oversupply) {
                    for (AVRequest avr : requests) {
                        RoboTaxi closest = unassignedRoboTaxis.getClosest(getLocation(avr));
                        if (closest != null) {
                            setRoboTaxiPickup(closest, avr);
                            unassignedRoboTaxis.remove(closest);
                            requestMaintainer.remove(avr);
                        }
                    }
                    /** undersupply case */
                } else {
                    for (RoboTaxi robotaxi : robotaxisDivertable) {
                        AVRequest closest = requestMaintainer.getClosest(robotaxi.getDivertableLocation().getFromNode().getCoord());
                        if (closest != null) {
                            setRoboTaxiPickup(robotaxi, closest);
                            unassignedRoboTaxis.remove(robotaxi);
                            requestMaintainer.remove(closest);
                        }
                    }
                }
            }
        }
    }

    /** @param request
     * @return {@link Coord} with {@link AVRequest} location */
    /* package */ Coord getLocation(AVRequest request) {
        return request.getFromLink().getFromNode().getCoord();
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ Coord getRoboTaxiLoc(RoboTaxi roboTaxi) {
        return roboTaxi.getDivertableLocation().getCoord();
    }

    public static class Factory implements AVDispatcherFactory {
        @Inject
        @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        @Inject
        @Named(AVModule.AV_MODE)
        private Network network;

        @Inject
        private Config config;

        @Inject
        private MatsimAmodeusDatabase db;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            return new DemandSupplyBalancingDispatcher( //
                    config, avconfig, travelTime, //
                    router, eventsManager, network, db);
        }
    }
}