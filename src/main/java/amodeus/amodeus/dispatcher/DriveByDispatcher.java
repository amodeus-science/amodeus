/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.DispatcherUtils;
import amodeus.amodeus.dispatcher.core.RebalancingDispatcher;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.util.DrivebyRequestStopper;
import amodeus.amodeus.net.MatsimAmodeusDatabase;

/** Dispatcher sends vehicles to random links in the network and lets them pickup
 * any customers which are waiting along the road. */
public class DriveByDispatcher extends RebalancingDispatcher {
    private final List<Link> links;
    private final double rebPos = 0.99;
    private final Random randGen = new Random(1234);
    private final int rebalancingPeriod;
    private int total_abortTrip = 0;

    private DriveByDispatcher(Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, EventsManager eventsManager, //
            Network network, MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy, RoboTaxiUsageType.SINGLEUSED);
        links = new ArrayList<>(network.getLinks().values());
        Collections.shuffle(links, randGen);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        rebalancingPeriod = dispatcherConfig.getRebalancingPeriod(120);
    }

    @Override
    public void redispatch(double now) {

        // stop all vehicles which are driving by an open request
        total_abortTrip += DrivebyRequestStopper //
                .stopDrivingBy(DispatcherUtils.getPassengerRequestsAtLinks(getPassengerRequests()), getDivertableRoboTaxis(),
                        (rt, avr) -> setRoboTaxiPickup(rt, avr, Double.NaN, Double.NaN))
                .size();

        // send vehicles to travel around the city to random links (random
        // loitering)
        final long round_now = Math.round(now);
        if (round_now % rebalancingPeriod == 0 && 0 < getPassengerRequests().size())
            for (RoboTaxi roboTaxi : getDivertableRoboTaxis())
                if (rebPos > randGen.nextDouble())
                    setRoboTaxiRebalance(roboTaxi, pollNextDestination());
    }

    private Link pollNextDestination() {
        int index = randGen.nextInt(links.size());
        return links.get(index);
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s AT=%5d", //
                super.getInfoLine(), //
                total_abortTrip //
        );
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);

            return new DriveByDispatcher(config, operatorConfig, travelTime, router, eventsManager, network, db, rebalancingStrategy);
        }
    }

}