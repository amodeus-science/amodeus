package org.matsim.amodeus.framework;

import java.util.Map;

import org.matsim.amodeus.analysis.AnalysisOutputListener;
import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusGenerator;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.components.dispatcher.multi_od_heuristic.MultiODHeuristic;
import org.matsim.amodeus.components.dispatcher.single_fifo.SingleFIFODispatcher;
import org.matsim.amodeus.components.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import org.matsim.amodeus.components.generator.PopulationDensityGenerator;
import org.matsim.amodeus.components.router.DefaultAmodeusRouter;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.framework.registry.DispatcherRegistry;
import org.matsim.amodeus.framework.registry.GeneratorRegistry;
import org.matsim.amodeus.framework.registry.RouterRegistry;
import org.matsim.amodeus.routing.AmodeusRouteFactory;
import org.matsim.amodeus.routing.interaction.ClosestLinkInteractionFinder;
import org.matsim.amodeus.routing.interaction.LinkAttributeInteractionFinder;
import org.matsim.amodeus.scoring.AmodeusScoringModule;
import org.matsim.amodeus.waiting_time.StandardWaitingTimeFactory;
import org.matsim.amodeus.waiting_time.WaitingTimeFactory;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class AVModule extends AbstractModule {
    @Override
    public void install() {
        bind(AmodeusRouteFactory.class);

        configureDispatchmentStrategies();
        configureGeneratorStrategies();
        configureInteractionFinders();

        AVUtils.registerRouterFactory(binder(), DefaultAmodeusRouter.TYPE, DefaultAmodeusRouter.Factory.class);

        addControlerListenerBinding().to(AnalysisOutputListener.class);

        for (AmodeusModeConfig modeConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            install(new AVModeModule(modeConfig));
        }

        bind(StandardWaitingTimeFactory.class);
        bind(WaitingTimeFactory.class).to(StandardWaitingTimeFactory.class);

        if (AmodeusConfigGroup.get(getConfig()).getUseScoring()) {
            install(new AmodeusScoringModule());
        }
    }

    @Provides
    @Singleton
    public DispatcherRegistry provideDispatcherRegistry(Map<String, AmodeusDispatcher.AVDispatcherFactory> components) {
        return new DispatcherRegistry(components);
    }

    @Provides
    @Singleton
    public GeneratorRegistry provideGeneratorRegistry(Map<String, AmodeusGenerator.AVGeneratorFactory> components) {
        return new GeneratorRegistry(components);
    }

    @Provides
    @Singleton
    public RouterRegistry provideRouterRegistry(Map<String, AmodeusRouter.Factory> components) {
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
