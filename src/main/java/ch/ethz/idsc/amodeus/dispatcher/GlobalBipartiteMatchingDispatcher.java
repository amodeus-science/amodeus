/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfig;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatchingUtils;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.router.AVRouter;

/** Dispatcher repeatedly solves a bipartite matching problem to match available vehicles and open requests.
 * The problem can either be solved using networkdistance or Euclidean distance. Currently network
 * distance is enabled. */
public class GlobalBipartiteMatchingDispatcher extends UniversalDispatcher {

    private final int dispatchPeriod;
    private Tensor printVals = Tensors.empty();
    private final DistanceFunction distanceFunction;
    private final Network network;
    private final BipartiteMatchingUtils bipartiteMatchingUtils;

    private GlobalBipartiteMatchingDispatcher(Network network, Config config, //
            AVDispatcherConfig avDispatcherConfig, TravelTime travelTime, //
            AVRouter router, EventsManager eventsManager) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager);
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        DistanceHeuristics distanceHeuristics = //
                dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        bipartiteMatchingUtils = new BipartiteMatchingUtils(network);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        distanceFunction = distanceHeuristics.getDistanceFunction(network);
        this.network = network;
    }

    @Override
    public void redispatch(double now) {
        final long round_now = Math.round(now);
        if (round_now % dispatchPeriod == 0) {
            printVals = bipartiteMatchingUtils.executePickup(this, getDivertableRoboTaxis(), //
                    getAVRequests(), distanceFunction, network);
        }
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s H=%s", //
                super.getInfoLine(), //
                printVals.toString() /** This is where Dispatcher@ V... R... MR.. H is printed on console */
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
            return new GlobalBipartiteMatchingDispatcher( //
                    network, config, avconfig, travelTime, router, eventsManager);
        }
    }
}
