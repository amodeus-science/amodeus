package org.matsim.amodeus.drt;

import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.components.router.RouterShutdownListener;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.framework.VirtualNetworkModeModule;
import org.matsim.amodeus.framework.registry.RouterRegistry;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class AmodeusDrtModeModule extends AbstractDvrpModeModule {
    private final AmodeusModeConfig config;

    public AmodeusDrtModeModule(AmodeusModeConfig config) {
        super(config.getMode());
        this.config = config;
    }

    @Override
    public void install() {
        bindModal(AmodeusModeConfig.class).toInstance(config);

        bindModal(TravelTime.class).to(Key.get(TravelTime.class, Names.named(DvrpTravelTimeModule.DVRP_ESTIMATED)));

        bindModal(RouterShutdownListener.class).toProvider(modalProvider(getter -> {
            return new RouterShutdownListener(getter.getModal(AmodeusRouter.class));
        })).in(Singleton.class);
        addControlerListenerBinding().to(modalKey(RouterShutdownListener.class));

        bindModal(AmodeusRouter.class).toProvider(modalProvider(getter -> {
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);
            String routerName = operatorConfig.getRouterConfig().getType();

            AmodeusRouter.Factory factory = getter.get(RouterRegistry.class).get(routerName);
            return factory.createRouter(getter);
        })).in(Singleton.class);

        install(new VirtualNetworkModeModule(config));
    }
}
