package ch.ethz.matsim.av.framework;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.passenger.DefaultPassengerRequestValidator;
import org.matsim.contrib.dvrp.passenger.PassengerRequestValidator;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.InteractionFinderConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVOperatorFactory;
import ch.ethz.matsim.av.financial.PriceCalculator;
import ch.ethz.matsim.av.framework.registry.RouterRegistry;
import ch.ethz.matsim.av.network.AVNetworkFilter;
import ch.ethz.matsim.av.network.AVNetworkProvider;
import ch.ethz.matsim.av.replanning.AVOperatorChoiceStrategy;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.matsim.av.router.AVRouterShutdownListener;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.routing.AVRouteFactory;
import ch.ethz.matsim.av.routing.AVRoutingModule;
import ch.ethz.matsim.av.routing.interaction.AVInteractionFinder;
import ch.ethz.matsim.av.waiting_time.WaitingTime;
import ch.ethz.matsim.av.waiting_time.WaitingTimeModeModule;

public class AVModeModule extends AbstractDvrpModeModule {
    private final Id<AVOperator> operatorId;

    public AVModeModule(Id<AVOperator> operatorId, String mode) {
        super(mode);
        this.operatorId = operatorId;
    }

    @Override
    public void install() {
        DvrpModes.registerDvrpMode(binder(), getMode());

        // Operator (TODO: remove)
        bindModal(AVOperator.class).toProvider(new OperatorProvider(operatorId, getMode())).in(Singleton.class);

        // Network
        bindModal(Network.class).toProvider(new NetworkProvider(getMode())).in(Singleton.class);

        // Routing module
        bindModal(AVRoutingModule.class).toProvider(new RoutingModuleProvider(getMode()));
        bindModal(AVInteractionFinder.class).toProvider(new InteractionFinderProvider(getMode())).in(Singleton.class);
        addRoutingModuleBinding(getMode()).to(modalKey(AVRoutingModule.class));

        // DVRP dynamics
        bindModal(PassengerRequestValidator.class).toInstance(new DefaultPassengerRequestValidator());
        // bindModal(AVRouter.class).toProvider(new RouterProvider(getMode()));
        bindModal(VehicleType.class).toProvider(new VehicleTypeProvider(getMode())).in(Singleton.class);
        bindModal(TravelTime.class).to(Key.get(TravelTime.class, Names.named(DvrpTravelTimeModule.DVRP_ESTIMATED)));

        install(new WaitingTimeModeModule(operatorId, getMode()));

        AVConfigGroup config = AVConfigGroup.getOrCreate(getConfig());
        bindModal(OperatorConfig.class).toInstance(config.getOperatorConfig(operatorId));

        bindModal(AVRouterShutdownListener.class).toProvider(modalProvider(getter -> {
            return new AVRouterShutdownListener(getter.getModal(AVRouter.class));
        })).in(Singleton.class);
        addControlerListenerBinding().to(modalKey(AVRouterShutdownListener.class));

        bindModal(AVRouter.class).toProvider(modalProvider(getter -> {
            OperatorConfig operatorConfig = getter.getModal(OperatorConfig.class);
            String routerName = operatorConfig.getRouterConfig().getType();

            AVRouter.Factory factory = getter.get(RouterRegistry.class).get(routerName);
            return factory.createRouter(getter);
        })).in(Singleton.class);
    }

    @Provides
    RouteFactories provideRouteFactories(AVRouteFactory routeFactory) {
        // TODO: Is there a smarter way now?
        RouteFactories factories = new RouteFactories();
        factories.setRouteFactory(AVRoute.class, routeFactory);
        return factories;
    }

    static private class NetworkProvider extends ModalProviders.AbstractProvider<Network> {
        @Inject
        AVConfigGroup config;

        @Inject
        AVNetworkFilter customFilter; // TODO: Make modal

        @Inject
        Network fullNetwork;

        NetworkProvider(String mode) {
            super(mode);
        }

        @Override
        public Network get() {
            OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);

            String allowedLinkMode = config.getAllowedLinkMode(); // TODO: Make modal (or implicit)
            String allowedLinkAttribute = operatorConfig.getAllowedLinkAttribute();

            boolean cleanNetwork = operatorConfig.getCleanNetwork();
            AVNetworkProvider provider = new AVNetworkProvider(allowedLinkMode, allowedLinkAttribute, cleanNetwork);

            return provider.apply(operatorConfig.getId(), fullNetwork, customFilter);
        }
    };

    static private class RoutingModuleProvider extends ModalProviders.AbstractProvider<AVRoutingModule> {
        @Inject
        AVOperatorChoiceStrategy choiceStrategy;

        @Inject
        AVRouteFactory routeFactory;

        @Inject
        PopulationFactory populationFactory;

        @Inject
        @Named("walk")
        RoutingModule walkRoutingModule;

        @Inject
        AVConfigGroup config;

        @Inject
        @Named("car")
        Provider<RoutingModule> roadRoutingModuleProvider;

        @Inject
        PriceCalculator priceCalculator; // TODO: Make modal.

        RoutingModuleProvider(String mode) {
            super(mode);
        }

        @Override
        public AVRoutingModule get() {
            OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);
            boolean predictRoute = operatorConfig.getPredictRouteTravelTime() || operatorConfig.getPredictRoutePrice();
            boolean useAccessEgress = config.getUseAccessEgress();

            AVInteractionFinder interactionFinder = getModalInstance(AVInteractionFinder.class);
            WaitingTime waitingTime = getModalInstance(WaitingTime.class);

            return new AVRoutingModule(choiceStrategy, routeFactory, interactionFinder, waitingTime, populationFactory, walkRoutingModule, useAccessEgress, predictRoute,
                    predictRoute ? roadRoutingModuleProvider.get() : null, priceCalculator, getMode());
        }
    };

    // TODO: I doubt that we really need this provider here. Remove operators -> modes.
    static private class OperatorProvider extends ModalProviders.AbstractProvider<AVOperator> {
        @Inject
        AVOperatorFactory factory;

        @Inject
        AVConfigGroup config;

        private final Id<AVOperator> operatorId;

        OperatorProvider(Id<AVOperator> operatorId, String mode) {
            super(mode);
            this.operatorId = operatorId;
        }

        @Override
        public AVOperator get() {
            return factory.createOperator(operatorId, config.getOperatorConfig(operatorId));
        }
    };

    static private class InteractionFinderProvider extends ModalProviders.AbstractProvider<AVInteractionFinder> {
        @Inject
        Map<String, AVInteractionFinder.AVInteractionFinderFactory> factories; // TODO: Can this become a multibinder?

        InteractionFinderProvider(String mode) {
            super(mode);
        }

        @Override
        public AVInteractionFinder get() {
            OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);

            InteractionFinderConfig interactionConfig = operatorConfig.getInteractionFinderConfig();
            AVInteractionFinder.AVInteractionFinderFactory factory = factories.get(interactionConfig.getType());

            if (factory == null) {
                throw new IllegalStateException("AVInteractionFinder with this type does not exist: " + interactionConfig.getType());
            }

            Network network = getModalInstance(Network.class);
            return factory.createInteractionFinder(operatorConfig, network);
        }
    };

    static private class VehicleTypeProvider extends ModalProviders.AbstractProvider<VehicleType> {
        @Inject
        Vehicles vehicles;

        VehicleTypeProvider(String mode) {
            super(mode);
        }

        @Override
        public VehicleType get() {
            OperatorConfig operatorConfig = getModalInstance(OperatorConfig.class);

            String vehicleTypeName = operatorConfig.getGeneratorConfig().getVehicleType();
            VehicleType vehicleType = null;

            if (vehicleTypeName == null) {
                vehicleType = VehicleUtils.getDefaultVehicleType();
            } else {
                vehicleType = vehicles.getVehicleTypes().get(Id.create(vehicleTypeName, VehicleType.class));

                if (vehicleType == null) {
                    throw new IllegalStateException(String.format("VehicleType '%s' does not exist for operator '%s'", vehicleType, operatorConfig.getId()));
                }
            }

            return vehicleType;
        }
    }
}
