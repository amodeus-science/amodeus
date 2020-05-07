package ch.ethz.matsim.av.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.analysis.simulation.AnalysisOutputListener;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.PricingConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVOperatorFactory;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.MultiODHeuristic;
import ch.ethz.matsim.av.dispatcher.single_fifo.SingleFIFODispatcher;
import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import ch.ethz.matsim.av.financial.PriceCalculator;
import ch.ethz.matsim.av.financial.StaticPriceCalculator;
import ch.ethz.matsim.av.framework.registry.DispatcherRegistry;
import ch.ethz.matsim.av.framework.registry.GeneratorRegistry;
import ch.ethz.matsim.av.framework.registry.RouterRegistry;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.generator.PopulationDensityGenerator;
import ch.ethz.matsim.av.network.AVNetworkFilter;
import ch.ethz.matsim.av.network.NullNetworkFilter;
import ch.ethz.matsim.av.replanning.AVOperatorChoiceStrategy;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.matsim.av.router.DefaultAVRouter;
import ch.ethz.matsim.av.routing.AVRouteFactory;
import ch.ethz.matsim.av.routing.interaction.ClosestLinkInteractionFinder;
import ch.ethz.matsim.av.routing.interaction.LinkAttributeInteractionFinder;
import ch.ethz.matsim.av.scoring.AVScoringFunctionFactory;
import ch.ethz.matsim.av.scoring.AVSubpopulationScoringParameters;
import ch.ethz.matsim.av.waiting_time.StandardWaitingTimeFactory;
import ch.ethz.matsim.av.waiting_time.WaitingTimeFactory;

public class AVModule extends AbstractModule {
    @Override
    public void install() {
        // Not modal!

        addPlanStrategyBinding("AVOperatorChoice").to(AVOperatorChoiceStrategy.class);

        bind(ScoringFunctionFactory.class).to(AVScoringFunctionFactory.class).asEagerSingleton();

        bind(AVOperatorFactory.class);
        bind(AVRouteFactory.class);

        configureDispatchmentStrategies();
        configureGeneratorStrategies();
        configureInteractionFinders();

        AVUtils.registerRouterFactory(binder(), DefaultAVRouter.TYPE, DefaultAVRouter.Factory.class);

        bind(PriceCalculator.class).to(StaticPriceCalculator.class);
        addControlerListenerBinding().to(AnalysisOutputListener.class);

        bind(AVSubpopulationScoringParameters.class);
        bind(AVNetworkFilter.class).to(NullNetworkFilter.class);

        for (OperatorConfig operatorConfig : AVConfigGroup.getOrCreate(getConfig()).getOperatorConfigs().values()) {
            install(new AVModeModule(operatorConfig.getId(), "av"));
        }

        bind(StandardWaitingTimeFactory.class);
        bind(WaitingTimeFactory.class).to(StandardWaitingTimeFactory.class);
    }

    @Provides
    @Singleton
    public NullNetworkFilter provideNullNetworkFilter() {
        return new NullNetworkFilter();
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
    public AVScoringFunctionFactory provideAVScoringFunctionFactory(Scenario scenario, ScoringParametersForPerson defaultParameters, AVSubpopulationScoringParameters avParameters,
            PriceCalculator priceCalculator) {
        List<String> modes = new LinkedList<>();

        /* for (OperatorConfig operatorConfig : AVConfigGroup.getOrCreate(getConfig()).getOperatorConfigs().values()) {
         * 
         * } */

        // TODO: Refactor this when we change to modes!
        modes.add("av");

        return new AVScoringFunctionFactory(scenario, defaultParameters, avParameters, priceCalculator, modes);
    }

    @Provides
    @Singleton
    public AVOperatorChoiceStrategy provideAVOperatorChoiceStrategy(AVConfigGroup config) {
        List<Id<AVOperator>> operatorIds = new ArrayList<>(config.getOperatorConfigs().keySet());
        List<String> modes = Arrays.asList("av"); // Refactor av

        return new AVOperatorChoiceStrategy(operatorIds, modes);
    }

    @Provides
    @Singleton
    public DispatcherRegistry provideDispatcherRegistry(Map<String, AVDispatcher.AVDispatcherFactory> components) {
        return new DispatcherRegistry(components);
    }

    @Provides
    @Singleton
    public GeneratorRegistry provideGeneratorRegistry(Map<String, AVGenerator.AVGeneratorFactory> components) {
        return new GeneratorRegistry(components);
    }

    @Provides
    @Singleton
    public RouterRegistry provideRouterRegistry(Map<String, AVRouter.Factory> components) {
        return new RouterRegistry(components);
    }

    private void configureDispatchmentStrategies() {
        bind(SingleFIFODispatcher.Factory.class);
        bind(SingleHeuristicDispatcher.Factory.class);
        bind(MultiODHeuristic.Factory.class);

        AVUtils.bindDispatcherFactory(binder(), SingleFIFODispatcher.TYPE).to(SingleFIFODispatcher.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), SingleHeuristicDispatcher.TYPE).to(SingleHeuristicDispatcher.Factory.class);
        AVUtils.bindDispatcherFactory(binder(), MultiODHeuristic.TYPE).to(MultiODHeuristic.Factory.class);
    }

    private void configureGeneratorStrategies() {
        bind(PopulationDensityGenerator.Factory.class);
        AVUtils.bindGeneratorFactory(binder(), PopulationDensityGenerator.TYPE).to(PopulationDensityGenerator.Factory.class);
    }

    private void configureInteractionFinders() {
        bind(ClosestLinkInteractionFinder.Factory.class);
        bind(LinkAttributeInteractionFinder.Factory.class);

        AVUtils.registerInteractionFinderFactory(binder(), ClosestLinkInteractionFinder.TYPE, ClosestLinkInteractionFinder.Factory.class);
        AVUtils.registerInteractionFinderFactory(binder(), LinkAttributeInteractionFinder.TYPE, LinkAttributeInteractionFinder.Factory.class);
    }
}
