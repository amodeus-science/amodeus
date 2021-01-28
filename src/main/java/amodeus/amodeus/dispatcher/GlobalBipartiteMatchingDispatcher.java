/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.core.RebalancingDispatcher;
import amodeus.amodeus.dispatcher.util.BipartiteMatcher;
import amodeus.amodeus.dispatcher.util.ConfigurableBipartiteMatcher;
import amodeus.amodeus.dispatcher.util.DistanceCost;
import amodeus.amodeus.dispatcher.util.DistanceHeuristics;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.routing.DistanceFunction;
import amodeus.amodeus.util.matsim.SafeConfig;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

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
            AmodeusRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy, RoboTaxiUsageType.SINGLEUSED);
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
                    getPassengerRequests(), distanceFunction, network);
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
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);
            
            return new GlobalBipartiteMatchingDispatcher(network, config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy);
        }
    }
}
