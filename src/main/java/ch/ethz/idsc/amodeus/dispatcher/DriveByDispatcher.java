/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfig;
import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherUtils;
import ch.ethz.idsc.amodeus.dispatcher.core.RebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.DrivebyRequestStopper;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.router.AVRouter;

/** Dispatcher sends vehicles to random links in the network and lets them pickup
 * any customers which are waiting along the road. */
public class DriveByDispatcher extends RebalancingDispatcher {
    private final List<Link> links;
    private final double rebPos = 0.99;
    private final Random randGen = new Random(1234);
    private final int rebalancingPeriod;
    private int total_abortTrip = 0;

    private DriveByDispatcher(//
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, //
            AVRouter router, //
            EventsManager eventsManager, //
            Network network) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager);
        links = new ArrayList<>(network.getLinks().values());
        Collections.shuffle(links, randGen);
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        rebalancingPeriod = dispatcherConfig.getRebalancingPeriod(120);
    }

    @Override
    public void redispatch(double now) {

        // stop all vehicles which are driving by an open request
        total_abortTrip += DrivebyRequestStopper //
                .stopDrivingBy(DispatcherUtils.getAVRequestsAtLinks(getAVRequests()), getDivertableRoboTaxis(), this::setRoboTaxiPickup).size();

        // send vehicles to travel around the city to random links (random
        // loitering)
        final long round_now = Math.round(now);
        if (round_now % rebalancingPeriod == 0 && 0 < getAVRequests().size()) {
            for (RoboTaxi roboTaxi : getDivertableRoboTaxis()) {
                if (rebPos > randGen.nextDouble()) {
                    setRoboTaxiRebalance(roboTaxi, pollNextDestination());
                }
            }
        }
    }

    private Link pollNextDestination() {
        int index = randGen.nextInt(links.size());
        Link link = links.get(index);
        return link;
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s AT=%5d", //
                super.getInfoLine(), //
                total_abortTrip //
        );
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

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            return new DriveByDispatcher(config, avconfig, travelTime, router, eventsManager, network);
        }
    }

}