package org.matsim.amodeus.framework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.components.dispatcher.multi_od_heuristic.MultiODHeuristic;
import org.matsim.amodeus.components.dispatcher.single_fifo.SingleFIFODispatcher;
import org.matsim.amodeus.components.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import org.matsim.amodeus.components.router.DefaultAmodeusRouter;
import org.matsim.amodeus.framework.registry.DispatcherRegistry;
import org.matsim.amodeus.framework.registry.RouterRegistry;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import amodeus.amodeus.data.ReferenceFrame;
import amodeus.amodeus.matsim.DispatcherModule;
import amodeus.amodeus.matsim.RouterModule;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;

public class AmodeusBaseModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusBaseModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        configureDispatchmentStrategies();
        configureRouters();

        addControlerListenerBinding().to(MatsimAmodeusDatabase.class);
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

    private void configureRouters() {
        AmodeusUtils.registerRouterFactory(binder(), DefaultAmodeusRouter.TYPE, DefaultAmodeusRouter.Factory.class);
        install(new RouterModule());
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
    public RouterRegistry provideRouterRegistry(Map<String, AmodeusRouter.Factory> components) {
        return new RouterRegistry(components);
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
