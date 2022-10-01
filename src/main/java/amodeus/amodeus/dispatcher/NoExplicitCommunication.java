/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher;

import java.util.Objects;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.modal.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.core.AbstractNoExplicitCommunication;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.TensorCoords;

/**
 * Arsie, Alessandro, Ketan Savla, and Emilio Frazzoli. "Efficient routing
 * algorithms for multiple
 * vehicles with no explicit communications." IEEE Transactions on Automatic
 * Control 54.10 (2009): 2302-2317. ,
 * Algorithm 1 "A control policy requiring no explicit communication"
 */
public class NoExplicitCommunication extends AbstractNoExplicitCommunication {

    private NoExplicitCommunication(Network network, Config config, //
            AmodeusModeConfig operatorConfig, TravelTime travelTime, //
            AmodeusRouter router, EventsManager eventsManager, MatsimAmodeusDatabase db,
            RebalancingStrategy rebalancingStrategy) {
        super(network, config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy);
    }

    @Override
    protected void redispatchIteration() {
        /** 1) if D(t) not empty, move towards nearest outstanding target */
        for (RoboTaxi roboTaxi : getDivertableRoboTaxis()) {
            if (getPassengerRequests().size() > 0) {
                PassengerRequest closest = requestMaintainer
                        .getClosest(TensorCoords.toTensor(roboTaxi.getDivertableLocation().getCoord()));
                /**
                 * here rebalance not pickup is chosen as in the policy, all
                 * agents move towards the open targets, i.e., there can be more than
                 * one agent moving towards a target
                 */
                if (Objects.nonNull(closest))
                    /** excessive computation is avoided if rebalancing command given only once */
                    if (!roboTaxi.getCurrentDriveDestination().equals(closest.getFromLink()))
                        setRoboTaxiRebalance(roboTaxi, closest.getFromLink());
            } else {
                /**
                 * move towards the point minimizing the average distance to targets
                 * serviced in the past by each agent
                 */
                Link link = weberMaintainers.get(roboTaxi).getClosestMinimizer(roboTaxi.getDivertableLocation());
                /** excessive computation is avoided if rebalancing command given only once */
                if (!roboTaxi.getCurrentDriveDestination().equals(link))
                    setRoboTaxiRebalance(roboTaxi, link);
            }
        }
    }

    public static class Factory implements AmodeusDispatcher.AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = (Config) inject.get(Config.class);
            MatsimAmodeusDatabase db = (MatsimAmodeusDatabase) inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = (EventsManager) inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = (AmodeusModeConfig) inject.getModal(AmodeusModeConfig.class);
            Network network = (Network) inject.getModal(Network.class);
            AmodeusRouter router = (AmodeusRouter) inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = (TravelTime) inject.getModal(TravelTime.class);
            RebalancingStrategy rebalancingStrategy = (RebalancingStrategy) inject.getModal(RebalancingStrategy.class);

            return new NoExplicitCommunication(network, config, operatorConfig, travelTime, router, eventsManager, db,
                    rebalancingStrategy);
        }
    }
}
