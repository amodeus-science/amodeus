package org.matsim.amodeus.framework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import amodeus.amodeus.data.ReferenceFrame;
import amodeus.amodeus.matsim.DispatcherModule;
import amodeus.amodeus.matsim.GeneratorModule;
import amodeus.amodeus.matsim.RouterModule;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;

public class AmodeusModule extends AbstractModule {
    private final MatsimAmodeusDatabase database;
    private final ScenarioOptions scenarioOptions;

    public AmodeusModule(MatsimAmodeusDatabase database, ScenarioOptions scenarioOptions) {
        this.database = database;
        this.scenarioOptions = scenarioOptions;
    }

    public AmodeusModule() {
        this(null, null);
    }

    @Override
    public void install() {
        bind(AmodeusRouteFactory.class);

        configureDispatchmentStrategies();
        configureGeneratorStrategies();
        configureInteractionFinders();
        configureRouters();

        addControlerListenerBinding().to(AnalysisOutputListener.class);

        for (AmodeusModeConfig modeConfig : AmodeusConfigGroup.get(getConfig()).getModes().values()) {
            install(new AmodeusModeModule(modeConfig));
        }

        bind(StandardWaitingTimeFactory.class);
        bind(WaitingTimeFactory.class).to(StandardWaitingTimeFactory.class);

        if (AmodeusConfigGroup.get(getConfig()).getUseScoring()) {
            install(new AmodeusScoringModule());
        }

        addControlerListenerBinding().to(MatsimAmodeusDatabase.class);
    }

    @Provides
    @Singleton
    public ScenarioOptions provideScenarioOptions() throws IOException {
        if (scenarioOptions != null) {
            return scenarioOptions;
        } else {
            URL workingDirectory = ConfigGroup.getInputFileURL(getConfig().getContext(), ".");
            return new ScenarioOptions(new File(workingDirectory.getPath()), ScenarioOptionsBase.getDefault());
        }
    }

    @Provides
    @Singleton
    public MatsimAmodeusDatabase provideMatsimAmodeusDatabase(Network network) {
        return MatsimAmodeusDatabase.initialize(network, new IdentityReferenceFrame());
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

        AmodeusUtils.bindDispatcherFactory(binder(), SingleFIFODispatcher.TYPE).to(SingleFIFODispatcher.Factory.class);
        AmodeusUtils.bindDispatcherFactory(binder(), SingleHeuristicDispatcher.TYPE).to(SingleHeuristicDispatcher.Factory.class);
        AmodeusUtils.bindDispatcherFactory(binder(), MultiODHeuristic.TYPE).to(MultiODHeuristic.Factory.class);

        install(new DispatcherModule());
    }

    private void configureGeneratorStrategies() {
        bind(PopulationDensityGenerator.Factory.class);
        AmodeusUtils.bindGeneratorFactory(binder(), PopulationDensityGenerator.TYPE).to(PopulationDensityGenerator.Factory.class);

        install(new GeneratorModule());
    }

    private void configureRouters() {
        AmodeusUtils.registerRouterFactory(binder(), DefaultAmodeusRouter.TYPE, DefaultAmodeusRouter.Factory.class);
        install(new RouterModule());
    }

    private void configureInteractionFinders() {
        bind(ClosestLinkInteractionFinder.Factory.class);
        bind(LinkAttributeInteractionFinder.Factory.class);

        AmodeusUtils.registerInteractionFinderFactory(binder(), ClosestLinkInteractionFinder.TYPE, ClosestLinkInteractionFinder.Factory.class);
        AmodeusUtils.registerInteractionFinderFactory(binder(), LinkAttributeInteractionFinder.TYPE, LinkAttributeInteractionFinder.Factory.class);
    }

    private class IdentityReferenceFrame implements ReferenceFrame {
        private final CoordinateTransformation coords_toWGS84 = new GeotoolsTransformation("EPSG:21781", "WGS84");
        private final CoordinateTransformation coords_fromWGS84 = new GeotoolsTransformation("WGS84", "EPSG:21781");

        @Override
        public CoordinateTransformation coords_fromWGS84() {
            return coords_fromWGS84;
        }

        @Override
        public CoordinateTransformation coords_toWGS84() {
            return coords_toWGS84;
        }
    }
}
