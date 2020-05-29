/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.util.Objects;

import org.matsim.amodeus.components.AVDispatcher;
import org.matsim.amodeus.components.AVRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.AbstractNoExplicitCommunication;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.VoronoiPartition;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;

/** Arsie, Alessandro, Ketan Savla, and Emilio Frazzoli. "Efficient routing algorithms for multiple
 * vehicles with no explicit communications." IEEE Transactions on Automatic Control 54.10 (2009): 2302-2317. ,
 * Algorithm 2 "A sensor based control policy" */
public class SBNoExplicitCommunication extends AbstractNoExplicitCommunication {
    private final VoronoiPartition<RoboTaxi> voronoiPartition;

    private SBNoExplicitCommunication(Network network, Config config, //
            AmodeusModeConfig operatorConfig, TravelTime travelTime, //
            AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(network, config, operatorConfig, travelTime, router, eventsManager, db);
        voronoiPartition = new VoronoiPartition<>(network, this::getRTLocation);
    }

    @Override
    protected void redispatchIteration() {
        /** update the {@link VoronoiPartition} */
        if (getRoboTaxis().size() > 0)
            voronoiPartition.update(getDivertableRoboTaxis());

        /** 1) if D(t) not empty, move towards nearest outstanding target */
        for (RoboTaxi roboTaxi : getDivertableRoboTaxis()) {
            PassengerRequest closest = requestMaintainer.getClosest( //
                    TensorCoords.toTensor(roboTaxi.getDivertableLocation().getCoord()));
            // /** as long as the {@link RoboTaxi} has never visited any target, move towards closest request */
            // if (!weberMaintainers.get(roboTaxi).visitedTargets()) {
            // if (Objects.nonNull(closest)) {
            // setRoboTaxiRebalance(roboTaxi, closest.getFromLink());
            // }
            // continue;
            // }
            /** move towards the closest target in the Voronoi region of the {@link RoboTaxi} */
            if (Objects.isNull(closest)) {
                /** move towards the point minimizing the average distance to targets
                 * serviced in the past by each agent */
                Link link = weberMaintainers.get(roboTaxi).getClosestMinimizer(roboTaxi.getDivertableLocation());
                /** excessive computation is avoided if rebalancing command given only once */
                if (!roboTaxi.getCurrentDriveDestination().equals(link))
                    setRoboTaxiRebalance(roboTaxi, link);
                continue;
            }
            if (voronoiPartition.of(roboTaxi).contains(closest.getFromLink())) {
                /** here rebalance not pickup is chosen as in the policy, all
                 * agents move towards the open targets, i.e., there can be more than
                 * one agent moving towards a target */
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

    private Coord getRTLocation(RoboTaxi roboTaxi) {
        return roboTaxi.getDivertableLocation().getCoord();
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

            return new SBNoExplicitCommunication(network, config, operatorConfig, travelTime, router, eventsManager, db);
        }
    }
}
