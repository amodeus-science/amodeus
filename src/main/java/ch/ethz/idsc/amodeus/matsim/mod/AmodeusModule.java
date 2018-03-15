/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Collection;

import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimPlugin;

public class AmodeusModule extends AbstractModule {
    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public Collection<AbstractQSimPlugin> provideQSimPlugins(Config config) {
        /* We construct the same QSim as for the AV package, but we replace the original
         * QSim plugin with an adjusted version. */

        Collection<AbstractQSimPlugin> plugins = new AVModule().provideQSimPlugins(config);
        plugins.removeIf(p -> p instanceof AVQSimPlugin);
        plugins.add(new AmodeusQSimPlugin(config));
        return plugins;
    }
}
