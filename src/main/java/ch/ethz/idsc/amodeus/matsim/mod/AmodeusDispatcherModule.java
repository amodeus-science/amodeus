/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import ch.ethz.idsc.amodeus.dispatcher.AdaptiveRealTimeRebalancingPolicy;
import ch.ethz.idsc.amodeus.dispatcher.DemandSupplyBalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.DriveByDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.FeedforwardFluidicRebalancingPolicy;
import ch.ethz.idsc.amodeus.dispatcher.GlobalBipartiteMatchingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.GlobalBipartiteMatchingDispatcherTemp;
import ch.ethz.matsim.av.framework.AVUtils;

public class AmodeusDispatcherModule extends AbstractModule {
    @Override
    public void install() {
        /** dispatchers for UniversalDispatcher */

        bind(DriveByDispatcher.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), DriveByDispatcher.class.getSimpleName()).to(DriveByDispatcher.Factory.class);

        bind(DemandSupplyBalancingDispatcher.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), DemandSupplyBalancingDispatcher.class.getSimpleName()).to(DemandSupplyBalancingDispatcher.Factory.class);

        bind(GlobalBipartiteMatchingDispatcher.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), GlobalBipartiteMatchingDispatcher.class.getSimpleName()).to(GlobalBipartiteMatchingDispatcher.Factory.class);

        bind(GlobalBipartiteMatchingDispatcherTemp.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), GlobalBipartiteMatchingDispatcherTemp.class.getSimpleName()).to(GlobalBipartiteMatchingDispatcherTemp.Factory.class);

        /** dispatchers for PartitionedDispatcher */

        bind(AdaptiveRealTimeRebalancingPolicy.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), AdaptiveRealTimeRebalancingPolicy.class.getSimpleName()).to(AdaptiveRealTimeRebalancingPolicy.Factory.class);

        bind(FeedforwardFluidicRebalancingPolicy.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), FeedforwardFluidicRebalancingPolicy.class.getSimpleName()).to(FeedforwardFluidicRebalancingPolicy.Factory.class);

    }
}
