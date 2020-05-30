package org.matsim.amodeus.framework;

import java.util.Map;

import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.components.router.RouterShutdownListener;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.InteractionFinderConfig;
import org.matsim.amodeus.framework.registry.RouterRegistry;
import org.matsim.amodeus.price_model.PriceModel;
import org.matsim.amodeus.price_model.StaticPriceModel;
import org.matsim.amodeus.routing.AmodeusRouteFactory;
import org.matsim.amodeus.routing.AmodeusRoutingModule;
import org.matsim.amodeus.routing.interaction.AmodeusInteractionFinder;
import org.matsim.amodeus.waiting_time.WaitingTime;
import org.matsim.amodeus.waiting_time.WaitingTimeEstimationModule;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.passenger.DefaultPassengerRequestValidator;
import org.matsim.contrib.dvrp.passenger.PassengerRequestValidator;
import org.matsim.contrib.dvrp.router.DvrpModeRoutingNetworkModule;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class AmodeusModeModule extends AbstractDvrpModeModule {
    private final AmodeusModeConfig modeConfig;

    public AmodeusModeModule(AmodeusModeConfig modeConfig) {
        super(modeConfig.getMode());
        this.modeConfig = modeConfig;
    }

    @Override
    public void install() {
        DvrpModes.registerDvrpMode(binder(), getMode());

        // Provide configuration to modal injectors
        bindModal(AmodeusModeConfig.class).toInstance(modeConfig);

        // Network
        install(new DvrpModeRoutingNetworkModule(getMode(), modeConfig.getUseModeFilteredSubnetwork()));

        // Routing module
        bindModal(AmodeusRoutingModule.class).toProvider(new RoutingModuleProvider(getMode()));
        bindModal(AmodeusInteractionFinder.class).toProvider(new InteractionFinderProvider(getMode())).in(Singleton.class);
        addRoutingModuleBinding(getMode()).to(modalKey(AmodeusRoutingModule.class));

        // DVRP dynamics
        bindModal(PassengerRequestValidator.class).toInstance(new DefaultPassengerRequestValidator());
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

        // Waiting time estimation
        install(new WaitingTimeEstimationModule(modeConfig));

        bindModal(PriceModel.class).toProvider(new PriceCalculatorProider(modeConfig));

        install(new VirtualNetworkModeModule(modeConfig));
    }

    static private class RoutingModuleProvider extends ModalProviders.AbstractProvider<AmodeusRoutingModule> {
        @Inject
        AmodeusRouteFactory routeFactory;

        @Inject
        PopulationFactory populationFactory;

        @Inject
        @Named("walk")
        RoutingModule walkRoutingModule;

        @Inject
        @Named("car")
        TravelTime travelTime;

        @Inject
        LeastCostPathCalculatorFactory routerFactory;

        RoutingModuleProvider(String mode) {
            super(mode);
        }

        @Override
        public AmodeusRoutingModule get() {
            AmodeusModeConfig modeConfig = getModalInstance(AmodeusModeConfig.class);
            boolean predictRoute = modeConfig.getPredictRouteTravelTime() || modeConfig.getPredictRoutePrice();
            boolean useAccessEgress = modeConfig.getUseAccessEgress();

            AmodeusInteractionFinder interactionFinder = getModalInstance(AmodeusInteractionFinder.class);
            WaitingTime waitingTime = getModalInstance(WaitingTime.class);
            PriceModel priceCalculator = getModalInstance(PriceModel.class);
            Network network = getModalInstance(Network.class);

            TravelDisutility travelDisutility = new OnlyTimeDependentTravelDisutilityFactory().createTravelDisutility(travelTime);
            LeastCostPathCalculator router = routerFactory.createPathCalculator(network, travelDisutility, travelTime);

            return new AmodeusRoutingModule(routeFactory, interactionFinder, waitingTime, populationFactory, walkRoutingModule, useAccessEgress, predictRoute, router,
                    priceCalculator, network, travelTime, getMode());
        }
    };

    static private class InteractionFinderProvider extends ModalProviders.AbstractProvider<AmodeusInteractionFinder> {
        @Inject
        Map<String, AmodeusInteractionFinder.AVInteractionFinderFactory> factories;

        InteractionFinderProvider(String mode) {
            super(mode);
        }

        @Override
        public AmodeusInteractionFinder get() {
            AmodeusModeConfig modeConfig = getModalInstance(AmodeusModeConfig.class);

            InteractionFinderConfig interactionConfig = modeConfig.getInteractionFinderConfig();
            AmodeusInteractionFinder.AVInteractionFinderFactory factory = factories.get(interactionConfig.getType());

            if (factory == null) {
                throw new IllegalStateException("AVInteractionFinder with this type does not exist: " + interactionConfig.getType());
            }

            Network network = getModalInstance(Network.class);
            return factory.createInteractionFinder(modeConfig, network);
        }
    };

    static class PriceCalculatorProider extends ModalProviders.AbstractProvider<PriceModel> {
        PriceCalculatorProider(AmodeusModeConfig modeConfig) {
            super(modeConfig.getMode());
        }

        @Override
        public PriceModel get() {
            AmodeusModeConfig modeConfig = getModalInstance(AmodeusModeConfig.class);
            return new StaticPriceModel(modeConfig.getPricingConfig());
        }
    }
}
