/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import ch.ethz.idsc.amodeus.routing.DefaultAStarLMRouter;
import ch.ethz.matsim.av.framework.AVUtils;

public class AmodeusRouterModule extends AbstractModule {
    @Override
    public void install() {

        /** here addtional standard routers can be included, e.g., as in commented, not functional
         * sample code below:
         * 
         * 
         * bind(MyOwnRouter.Factory.class);
         * AVUtils.bindRouterFactory(binder(), MyOwnRouter.class.getSimpleName()).to(MyOwnRouter.Factory.class); */
        bind(DefaultAStarLMRouter.Factory.class);
        AVUtils.bindRouterFactory(binder(), DefaultAStarLMRouter.class.getSimpleName()).to(DefaultAStarLMRouter.Factory.class);
    }
}
