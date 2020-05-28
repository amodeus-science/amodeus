/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.util.Objects;

import org.matsim.amodeus.components.AVDispatcher;
import org.matsim.amodeus.components.AVRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.AbstractNoExplicitCommunication;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;

/** Arsie, Alessandro, Ketan Savla, and Emilio Frazzoli. "Efficient routing algorithms for multiple
 * vehicles with no explicit communications." IEEE Transactions on Automatic Control 54.10 (2009): 2302-2317. ,
 * Algorithm 1 "A control policy requiring no explicit communication" */
public class NoExplicitCommunication extends AbstractNoExplicitCommunication {

    private NoExplicitCommunication(Network network, Config config, //
            AmodeusModeConfig operatorConfig, TravelTime travelTime, //
            AVRouter router, EventsManager eventsManager, MatsimAmodeusDatabase db) {
        super(network, config, operatorConfig, travelTime, router, eventsManager, db);
    }

    @Override
    protected void redispatchIteration() {
        /** 1) if D(t) not empty, move towards nearest outstanding target */
        for (RoboTaxi roboTaxi : getDivertableRoboTaxis()) {
            if (getAVRequests().size() > 0) {
                AVRequest closest = requestMaintainer.getClosest(TensorCoords.toTensor(roboTaxi.getDivertableLocation().getCoord()));
                /** here rebalance not pickup is chosen as in the policy, all
                 * agents move towards the open targets, i.e., there can be more than
                 * one agent moving towards a target */
                if (Objects.nonNull(closest))
                    /** excessive computation is avoided if rebalancing command given only once */
                    if (!roboTaxi.getCurrentDriveDestination().equals(closest.getFromLink()))
                        setRoboTaxiRebalance(roboTaxi, closest.getFromLink());
            } else {
                /** move towards the point minimizing the average distance to targets
                 * serviced in the past by each agent */
                Link link = weberMaintainers.get(roboTaxi).getClosestMinimizer(roboTaxi.getDivertableLocation());
                /** excessive computation is avoided if rebalancing command given only once */
                if (!roboTaxi.getCurrentDriveDestination().equals(link))
                    setRoboTaxiRebalance(roboTaxi, link);
            }
        }
    }

    public static class Factory implements AVDispatcher.AVDispatcherFactory {
        @Override
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AVRouter router = inject.getModal(AVRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            return new NoExplicitCommunication(network, config, operatorConfig, travelTime, router, eventsManager, db);
        }
    }
}
