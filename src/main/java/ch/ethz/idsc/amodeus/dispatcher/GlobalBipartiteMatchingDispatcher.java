/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfigWrapper;
import ch.ethz.idsc.amodeus.dispatcher.core.RebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.ConfigurableBipartiteMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceCost;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.router.AVRouter;

/** Dispatcher repeatedly solves a bipartite matching problem to match available vehicles and open requests.
 * The problem can either be solved using networkdistance or Euclidean distance. Currently network
 * distance is enabled.
 * 
 * This dispatcher is not a dispatcher with rebalancing functionality, it could also be derived from
 * the UniversalDispatcher, but in order to allow extended versions to use the setRoboTaxiRebalance
 * functionality, it was extended from the abstract RebalancingDispatcher. */
public class GlobalBipartiteMatchingDispatcher extends RebalancingDispatcher {

    private final int dispatchPeriod;
    private Tensor printVals = Tensors.empty();
    private final DistanceFunction distanceFunction;
    private final Network network;
    private final BipartiteMatcher bipartiteMatcher;

    protected GlobalBipartiteMatchingDispatcher(Network network, Config config, //
            AmodeusModeConfig operatorConfig, TravelTime travelTime, //
            AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, db);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        DistanceHeuristics distanceHeuristics = //
                dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        distanceFunction = distanceHeuristics.getDistanceFunction(network);
        this.network = network;
        /** matching algorithm - standard is a solution to the assignment problem with the Hungarian method */
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        bipartiteMatcher = new ConfigurableBipartiteMatcher(network, new DistanceCost(distanceFunction), //
                safeConfig);
    }

    @Override
    public void redispatch(double now) {
        final long round_now = Math.round(now);
        if (round_now % dispatchPeriod == 0)
            printVals = bipartiteMatcher.executePickup(this, getDivertableRoboTaxis(), //
                    getAVRequests(), distanceFunction, network);
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s H=%s", //
                super.getInfoLine(), //
                printVals.toString() /** This is where Dispatcher@ V... R... MR.. H is printed on console */
        );
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AVRouter router = inject.getModal(AVRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            return new GlobalBipartiteMatchingDispatcher(network, config, operatorConfig, travelTime, router, eventsManager, db);
        }
    }
}
