package ch.ethz.matsim.av.framework;

import java.util.Map;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.analysis.simulation.AnalysisOutputListener;
import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.MultiODHeuristic;
import ch.ethz.matsim.av.dispatcher.single_fifo.SingleFIFODispatcher;
import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import ch.ethz.matsim.av.framework.registry.DispatcherRegistry;
import ch.ethz.matsim.av.framework.registry.GeneratorRegistry;
import ch.ethz.matsim.av.framework.registry.RouterRegistry;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.generator.PopulationDensityGenerator;
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
        bind(ScoringFunctionFactory.class).to(AVScoringFunctionFactory.class).asEagerSingleton();

        bind(AVRouteFactory.class);

        configureDispatchmentStrategies();
        configureGeneratorStrategies();
        configureInteractionFinders();

        AVUtils.registerRouterFactory(binder(), DefaultAVRouter.TYPE, DefaultAVRouter.Factory.class);

        addControlerListenerBinding().to(AnalysisOutputListener.class);

        bind(AVSubpopulationScoringParameters.class);

        for (AmodeusModeConfig modeConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            install(new AVModeModule(modeConfig));
        }

        bind(StandardWaitingTimeFactory.class);
        bind(WaitingTimeFactory.class).to(StandardWaitingTimeFactory.class);
    }

    @Provides
    @Singleton
    public AVScoringFunctionFactory provideAVScoringFunctionFactory(Scenario scenario, ScoringParametersForPerson defaultParameters, AVSubpopulationScoringParameters avParameters,
            AmodeusConfigGroup config) {
        return new AVScoringFunctionFactory(scenario, defaultParameters, avParameters, config.getModes().keySet());
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
