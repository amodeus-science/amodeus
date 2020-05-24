package ch.ethz.matsim.av.framework;

import java.util.Map;

import org.matsim.api.core.v01.Id;
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
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.config.modal.InteractionFinderConfig;
import ch.ethz.matsim.av.financial.PriceCalculator;
import ch.ethz.matsim.av.financial.StaticPriceCalculator;
import ch.ethz.matsim.av.framework.registry.RouterRegistry;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.matsim.av.router.AVRouterShutdownListener;
import ch.ethz.matsim.av.routing.AVRouteFactory;
import ch.ethz.matsim.av.routing.AVRoutingModule;
import ch.ethz.matsim.av.routing.interaction.AVInteractionFinder;
import ch.ethz.matsim.av.waiting_time.WaitingTime;
import ch.ethz.matsim.av.waiting_time.WaitingTimeEstimationModule;

public class AVModeModule extends AbstractDvrpModeModule {
    private final AmodeusModeConfig modeConfig;

    public AVModeModule(AmodeusModeConfig modeConfig) {
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
        bindModal(AVRoutingModule.class).toProvider(new RoutingModuleProvider(getMode()));
        bindModal(AVInteractionFinder.class).toProvider(new InteractionFinderProvider(getMode())).in(Singleton.class);
        addRoutingModuleBinding(getMode()).to(modalKey(AVRoutingModule.class));

        // DVRP dynamics
        bindModal(PassengerRequestValidator.class).toInstance(new DefaultPassengerRequestValidator());
        // bindModal(AVRouter.class).toProvider(new RouterProvider(getMode()));
        bindModal(VehicleType.class).toProvider(new VehicleTypeProvider(getMode())).in(Singleton.class);
        bindModal(TravelTime.class).to(Key.get(TravelTime.class, Names.named(DvrpTravelTimeModule.DVRP_ESTIMATED)));

        bindModal(AVRouterShutdownListener.class).toProvider(modalProvider(getter -> {
            return new AVRouterShutdownListener(getter.getModal(AVRouter.class));
        })).in(Singleton.class);
        addControlerListenerBinding().to(modalKey(AVRouterShutdownListener.class));

        bindModal(AVRouter.class).toProvider(modalProvider(getter -> {
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);
            String routerName = operatorConfig.getRouterConfig().getType();

            AVRouter.Factory factory = getter.get(RouterRegistry.class).get(routerName);
            return factory.createRouter(getter);
        })).in(Singleton.class);

        // Waiting time estimation
        install(new WaitingTimeEstimationModule(modeConfig));

        bindModal(PriceCalculator.class).toProvider(new PriceCalculatorProider(modeConfig));
    }

    static private class RoutingModuleProvider extends ModalProviders.AbstractProvider<AVRoutingModule> {
        @Inject
        AVRouteFactory routeFactory;

        @Inject
        PopulationFactory populationFactory;

        @Inject
        @Named("walk")
        RoutingModule walkRoutingModule;

        @Inject
        @Named("car")
        Provider<RoutingModule> roadRoutingModuleProvider;

        RoutingModuleProvider(String mode) {
            super(mode);
        }

        @Override
        public AVRoutingModule get() {
            AmodeusModeConfig modeConfig = getModalInstance(AmodeusModeConfig.class);
            boolean predictRoute = modeConfig.getPredictRouteTravelTime() || modeConfig.getPredictRoutePrice();
            boolean useAccessEgress = modeConfig.getUseAccessEgress();

            AVInteractionFinder interactionFinder = getModalInstance(AVInteractionFinder.class);
            WaitingTime waitingTime = getModalInstance(WaitingTime.class);
            PriceCalculator priceCalculator = getModalInstance(PriceCalculator.class);

            return new AVRoutingModule(routeFactory, interactionFinder, waitingTime, populationFactory, walkRoutingModule, useAccessEgress, predictRoute,
                    predictRoute ? roadRoutingModuleProvider.get() : null, priceCalculator, getMode());
        }
    };

    static private class InteractionFinderProvider extends ModalProviders.AbstractProvider<AVInteractionFinder> {
        @Inject
        Map<String, AVInteractionFinder.AVInteractionFinderFactory> factories;

        InteractionFinderProvider(String mode) {
            super(mode);
        }

        @Override
        public AVInteractionFinder get() {
            AmodeusModeConfig modeConfig = getModalInstance(AmodeusModeConfig.class);

            InteractionFinderConfig interactionConfig = modeConfig.getInteractionFinderConfig();
            AVInteractionFinder.AVInteractionFinderFactory factory = factories.get(interactionConfig.getType());

            if (factory == null) {
                throw new IllegalStateException("AVInteractionFinder with this type does not exist: " + interactionConfig.getType());
            }

            Network network = getModalInstance(Network.class);
            return factory.createInteractionFinder(modeConfig, network);
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
            AmodeusModeConfig operatorConfig = getModalInstance(AmodeusModeConfig.class);

            String vehicleTypeName = operatorConfig.getGeneratorConfig().getVehicleType();
            VehicleType vehicleType = null;

            if (vehicleTypeName == null) {
                vehicleType = VehicleUtils.getDefaultVehicleType();
            } else {
                vehicleType = vehicles.getVehicleTypes().get(Id.create(vehicleTypeName, VehicleType.class));

                if (vehicleType == null) {
                    throw new IllegalStateException(String.format("VehicleType '%s' does not exist for mode '%s'", vehicleType, getMode()));
                }
            }

            return vehicleType;
        }
    }

    static class PriceCalculatorProider extends ModalProviders.AbstractProvider<PriceCalculator> {
        PriceCalculatorProider(AmodeusModeConfig modeConfig) {
            super(modeConfig.getMode());
        }

        @Override
        public PriceCalculator get() {
            AmodeusModeConfig modeConfig = getModalInstance(AmodeusModeConfig.class);
            return new StaticPriceCalculator(modeConfig.getPricingConfig());
        }
    }
}
