package ch.ethz.matsim.av.framework;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.passenger.DefaultPassengerRequestValidator;
import org.matsim.contrib.dvrp.passenger.PassengerRequestValidator;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import ch.ethz.matsim.av.analysis.simulation.AnalysisOutputListener;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.InteractionFinderConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.PricingConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVOperatorFactory;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.MultiODHeuristic;
import ch.ethz.matsim.av.dispatcher.single_fifo.SingleFIFODispatcher;
import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import ch.ethz.matsim.av.financial.PriceCalculator;
import ch.ethz.matsim.av.financial.StaticPriceCalculator;
import ch.ethz.matsim.av.generator.PopulationDensityGenerator;
import ch.ethz.matsim.av.network.AVNetworkFilter;
import ch.ethz.matsim.av.network.AVNetworkProvider;
import ch.ethz.matsim.av.network.NullNetworkFilter;
import ch.ethz.matsim.av.replanning.AVOperatorChoiceStrategy;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.matsim.av.router.AVRouterShutdownListener;
import ch.ethz.matsim.av.router.DefaultAVRouter;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.routing.AVRouteFactory;
import ch.ethz.matsim.av.routing.AVRoutingModule;
import ch.ethz.matsim.av.routing.interaction.AVInteractionFinder;
import ch.ethz.matsim.av.routing.interaction.ClosestLinkInteractionFinder;
import ch.ethz.matsim.av.routing.interaction.LinkAttributeInteractionFinder;
import ch.ethz.matsim.av.scoring.AVScoringFunctionFactory;
import ch.ethz.matsim.av.scoring.AVSubpopulationScoringParameters;
import ch.ethz.matsim.av.waiting_time.WaitingTime;
import ch.ethz.matsim.av.waiting_time.WaitingTimeModule;

public class AVModule extends AbstractModule {
	final static public String AV_MODE = "av";
	final static Logger log = Logger.getLogger(AVModule.class);

	private final boolean addQSimModule;

	public AVModule() {
		addQSimModule = true;
	}

	// Only for compatibility with Amodeus
	public AVModule(boolean addQSimModule) {
		this.addQSimModule = addQSimModule;
	}

	@Override
	public void install() {
		DvrpModes.registerDvrpMode(binder(), AV_MODE);
		bind(DvrpModes.key(Network.class, AV_MODE)).to(Network.class);

		bind(DvrpModes.key(PassengerRequestValidator.class, AV_MODE))
				.toInstance(new DefaultPassengerRequestValidator());

		if (addQSimModule) {
			installQSimModule(new AVQSimModule());
		}

		addRoutingModuleBinding(AV_MODE).to(AVRoutingModule.class);
		bind(ScoringFunctionFactory.class).to(AVScoringFunctionFactory.class).asEagerSingleton();

		bind(AVOperatorChoiceStrategy.class);
		addPlanStrategyBinding("AVOperatorChoice").to(AVOperatorChoiceStrategy.class);

		// Bind the AV travel time to the DVRP estimated travel time
		bind(TravelTime.class).annotatedWith(Names.named(AVModule.AV_MODE))
				.to(Key.get(TravelTime.class, Names.named(DvrpTravelTimeModule.DVRP_ESTIMATED)));

		bind(VehicleType.class).annotatedWith(Names.named(AVModule.AV_MODE))
				.toInstance(VehicleUtils.getDefaultVehicleType());

		bind(AVOperatorFactory.class);
		bind(AVRouteFactory.class);
		addRoutingModuleBinding(AV_MODE).to(AVRoutingModule.class);

		configureDispatchmentStrategies();
		configureGeneratorStrategies();
		configureInteractionFinders();

		addControlerListenerBinding().to(AVRouterShutdownListener.class);
		AVUtils.registerRouterFactory(binder(), DefaultAVRouter.TYPE, DefaultAVRouter.Factory.class);

		bind(AVSubpopulationScoringParameters.class);
		bind(AVNetworkFilter.class).to(NullNetworkFilter.class);

		install(new WaitingTimeModule());

		bind(PriceCalculator.class).to(StaticPriceCalculator.class);
		addControlerListenerBinding().to(AnalysisOutputListener.class);
	}

	private void configureDispatchmentStrategies() {
		bind(SingleFIFODispatcher.Factory.class);
		bind(SingleHeuristicDispatcher.Factory.class);
		bind(MultiODHeuristic.Factory.class);

		AVUtils.bindDispatcherFactory(binder(), SingleFIFODispatcher.TYPE).to(SingleFIFODispatcher.Factory.class);
		AVUtils.bindDispatcherFactory(binder(), SingleHeuristicDispatcher.TYPE)
				.to(SingleHeuristicDispatcher.Factory.class);
		AVUtils.bindDispatcherFactory(binder(), MultiODHeuristic.TYPE).to(MultiODHeuristic.Factory.class);
	}

	private void configureGeneratorStrategies() {
		bind(PopulationDensityGenerator.Factory.class);
		AVUtils.bindGeneratorFactory(binder(), PopulationDensityGenerator.TYPE)
				.to(PopulationDensityGenerator.Factory.class);
	}

	private void configureInteractionFinders() {
		bind(ClosestLinkInteractionFinder.Factory.class);
		bind(LinkAttributeInteractionFinder.Factory.class);

		AVUtils.registerInteractionFinderFactory(binder(), ClosestLinkInteractionFinder.TYPE,
				ClosestLinkInteractionFinder.Factory.class);
		AVUtils.registerInteractionFinderFactory(binder(), LinkAttributeInteractionFinder.TYPE,
				LinkAttributeInteractionFinder.Factory.class);
	}

	@Provides
	RouteFactories provideRouteFactories(AVRouteFactory routeFactory) {
		RouteFactories factories = new RouteFactories();
		factories.setRouteFactory(AVRoute.class, routeFactory);
		return factories;
	}

	@Provides
	@Singleton
	Map<Id<AVOperator>, AVOperator> provideOperators(AVConfigGroup config, AVOperatorFactory factory,
			PlansCalcRouteConfigGroup routeConfig) {
		Map<Id<AVOperator>, AVOperator> operators = new HashMap<>();

		for (OperatorConfig oc : config.getOperatorConfigs().values()) {
			if (oc.getPredictRouteTravelTime() && routeConfig.getRoutingRandomness() > 0.0) {
				throw new IllegalStateException(
						"Can only run AV extension with routingRandomness = 0.0 if travel times should be predicted!");
			}

			operators.put(oc.getId(), factory.createOperator(oc.getId(), oc));
		}

		return operators;
	}

	@Provides
	@Singleton
	public Map<Id<AVOperator>, AVRouter> provideRouters(Map<Id<AVOperator>, AVOperator> operators,
			Map<String, AVRouter.Factory> factories, Map<Id<AVOperator>, Network> networks) {
		Map<Id<AVOperator>, AVRouter> routers = new HashMap<>();

		for (AVOperator operator : operators.values()) {
			String routerName = operator.getConfig().getRouterConfig().getType();

			if (!factories.containsKey(routerName)) {
				throw new IllegalStateException("Router '" + routerName + "' is not registered");
			}

			Network network = networks.get(operator.getId());

			routers.put(operator.getId(),
					factories.get(routerName).createRouter(operator.getConfig().getRouterConfig(), network));
		}

		return routers;
	}

	@Provides
	public AVRoutingModule provideAVRoutingModule(AVOperatorChoiceStrategy choiceStrategy, AVRouteFactory routeFactory,
			Map<Id<AVOperator>, AVInteractionFinder> interactionFinders, Map<Id<AVOperator>, WaitingTime> waitingTimes,
			PopulationFactory populationFactory, @Named("walk") RoutingModule walkRoutingModule, AVConfigGroup config,
			@Named("car") Provider<RoutingModule> roadRoutingModuleProvider, PriceCalculator priceCalculator) {
		Map<Id<AVOperator>, Boolean> predictRouteTravelTime = new HashMap<>();
		boolean needsRoutingModule = false;

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			predictRouteTravelTime.put(operatorConfig.getId(), operatorConfig.getPredictRouteTravelTime());
			needsRoutingModule |= operatorConfig.getPredictRouteTravelTime();
		}

		return new AVRoutingModule(choiceStrategy, routeFactory, interactionFinders, waitingTimes, populationFactory,
				walkRoutingModule, config.getUseAccessEgress(), predictRouteTravelTime,
				needsRoutingModule ? roadRoutingModuleProvider.get() : null, priceCalculator);
	}

	@Provides
	@Singleton
	public Map<Id<AVOperator>, Network> provideNetworks(AVConfigGroup config, Network fullNetwork,
			AVNetworkFilter customFilter) {
		String allowedLinkMode = config.getAllowedLinkMode();
		Map<Id<AVOperator>, Network> networks = new HashMap<>();

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			String allowedLinkAttribute = operatorConfig.getAllowedLinkAttribute();
			boolean cleanNetwork = operatorConfig.getCleanNetwork();

			AVNetworkProvider provider = new AVNetworkProvider(allowedLinkMode, allowedLinkAttribute, cleanNetwork);

			Network operatorNetwork = provider.apply(operatorConfig.getId(), fullNetwork, customFilter);
			networks.put(operatorConfig.getId(), operatorNetwork);
		}

		return networks;
	}

	@Provides
	@Singleton
	public NullNetworkFilter provideNullNetworkFilter() {
		return new NullNetworkFilter();
	}

	@Provides
	@Singleton
	public Map<Id<AVOperator>, AVInteractionFinder> provideInteractionFinders(AVConfigGroup config,
			Map<String, AVInteractionFinder.AVInteractionFinderFactory> factories,
			Map<Id<AVOperator>, Network> networks) {
		Map<Id<AVOperator>, AVInteractionFinder> finders = new HashMap<>();

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			InteractionFinderConfig interactionConfig = operatorConfig.getInteractionFinderConfig();
			AVInteractionFinder.AVInteractionFinderFactory factory = factories.get(interactionConfig.getType());

			if (factory == null) {
				throw new IllegalStateException(
						"AVInteractionFinder with this type does not exist: " + interactionConfig.getType());
			}

			Network network = networks.get(operatorConfig.getId());

			AVInteractionFinder finder = factory.createInteractionFinder(operatorConfig, network);
			finders.put(operatorConfig.getId(), finder);
		}

		return finders;
	}

	@Provides
	@Singleton
	public StaticPriceCalculator provideStaticPriceCalculator(AVConfigGroup config) {
		Map<Id<AVOperator>, PricingConfig> pricingConfigs = new HashMap<>();

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			pricingConfigs.put(operatorConfig.getId(), operatorConfig.getPricingConfig());
		}

		return new StaticPriceCalculator(pricingConfigs);
	}

	@Provides
	@Singleton
	public Map<Id<AVOperator>, VehicleType> provideVehicleTypes(AVConfigGroup config, Vehicles vehicles) {
		Map<Id<AVOperator>, VehicleType> vehicleTypes = new HashMap<>();

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			String vehicleTypeName = operatorConfig.getGeneratorConfig().getVehicleType();
			VehicleType vehicleType = null;

			if (vehicleTypeName == null) {
				vehicleType = VehicleUtils.getDefaultVehicleType();
			} else {
				vehicleType = vehicles.getVehicleTypes().get(Id.create(vehicleTypeName, VehicleType.class));

				if (vehicleType == null) {
					throw new IllegalStateException(String.format("VehicleType '%s' does not exist for operator '%s'",
							vehicleType, operatorConfig.getId()));
				}
			}

			vehicleTypes.put(operatorConfig.getId(), vehicleType);
		}

		return vehicleTypes;
	}
}
