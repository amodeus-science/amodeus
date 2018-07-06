package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

public class AmodeusRouterModule extends AbstractModule {
    @Override
    public void install() {

        /** here addtional standard routers can be included, e.g., as in commented, not functional
         * sample code below:
         * 
         * 
         * bind(MyOwnRouter.Factory.class);
         * AVUtils.bindRouterFactory(binder(), MyOwnRouter.class.getSimpleName()).to(MyOwnRouter.Factory.class); */
    }
}
