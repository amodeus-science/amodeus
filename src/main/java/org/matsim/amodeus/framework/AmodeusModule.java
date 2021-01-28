package org.matsim.amodeus.framework;

import java.util.Map;

import org.matsim.amodeus.analysis.AnalysisOutputListener;
import org.matsim.amodeus.components.AmodeusGenerator;
import org.matsim.amodeus.components.generator.PopulationDensityGenerator;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.framework.registry.GeneratorRegistry;
import org.matsim.amodeus.routing.AmodeusRouteFactory;
import org.matsim.amodeus.routing.interaction.ClosestLinkInteractionFinder;
import org.matsim.amodeus.routing.interaction.LinkAttributeInteractionFinder;
import org.matsim.amodeus.scoring.AmodeusScoringModule;
import org.matsim.amodeus.waiting_time.StandardWaitingTimeFactory;
import org.matsim.amodeus.waiting_time.WaitingTimeFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import amodeus.amodeus.matsim.GeneratorModule;
import amodeus.amodeus.options.ScenarioOptions;

public class AmodeusModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    public AmodeusModule() {
        this(null);
    }

    @Override
    public void install() {
        install(new AmodeusBaseModule(scenarioOptions));
        bind(AmodeusRouteFactory.class);

        configureGeneratorStrategies();
        configureInteractionFinders();

        addControlerListenerBinding().to(AnalysisOutputListener.class);

        for (AmodeusModeConfig modeConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            install(new AmodeusModeModule(modeConfig));
        }

        bind(StandardWaitingTimeFactory.class);
        bind(WaitingTimeFactory.class).to(StandardWaitingTimeFactory.class);

        if (AmodeusConfigGroup.get(getConfig()).getUseScoring()) {
            install(new AmodeusScoringModule());
        }

        if (getConfig().getModules().containsKey(DrtConfigGroup.GROUP_NAME)) {
            throw new IllegalStateException("Can not use native Amodeus and DRT at the same time!");
        }
    }

    @Provides
    @Singleton
    public GeneratorRegistry provideGeneratorRegistry(Map<String, AmodeusGenerator.AVGeneratorFactory> components) {
        return new GeneratorRegistry(components);
    }

    private void configureGeneratorStrategies() {
        bind(PopulationDensityGenerator.Factory.class);
        AmodeusUtils.bindGeneratorFactory(binder(), PopulationDensityGenerator.TYPE).to(PopulationDensityGenerator.Factory.class);

        install(new GeneratorModule());
    }

    private void configureInteractionFinders() {
        bind(ClosestLinkInteractionFinder.Factory.class);
        bind(LinkAttributeInteractionFinder.Factory.class);

        AmodeusUtils.registerInteractionFinderFactory(binder(), ClosestLinkInteractionFinder.TYPE, ClosestLinkInteractionFinder.Factory.class);
        AmodeusUtils.registerInteractionFinderFactory(binder(), LinkAttributeInteractionFinder.TYPE, LinkAttributeInteractionFinder.Factory.class);
    }
}
