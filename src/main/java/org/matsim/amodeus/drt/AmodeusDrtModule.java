package org.matsim.amodeus.drt;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.components.router.RouterShutdownListener;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.framework.registry.RouterRegistry;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import amodeus.amodeus.data.ReferenceFrame;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;

public class AmodeusDrtModule extends AbstractDvrpModeModule {
    private final AmodeusModeConfig config;

    public AmodeusDrtModule(AmodeusModeConfig config) {
        super(config.getMode());
        this.config = config;
    }

    @Override
    public void install() {
        bindModal(AmodeusModeConfig.class).toInstance(config);

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
    }

    @Provides
    @Singleton
    public ScenarioOptions provideScenarioOptions() throws IOException {
        URL workingDirectory = ConfigGroup.getInputFileURL(getConfig().getContext(), ".");
        return new ScenarioOptions(new File(workingDirectory.getPath()), ScenarioOptionsBase.getDefault());
    }

    @Provides
    @Singleton
    public MatsimAmodeusDatabase provideMatsimAmodeusDatabase(Network network) {
        return MatsimAmodeusDatabase.initialize(network, new DrtReferenceFrame());
    }

    private class DrtReferenceFrame implements ReferenceFrame {
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
