/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Collection;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

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

    @Provides
    @Singleton
    @Named(DvrpModule.DVRP_ROUTING)
    public Network provideAVNetwork(Network fullNetwork) {
        /* TODO Here we provide the FULL network with public transit links etc., because this is how Amodeus has been set up initially. This was not a problem,
         * since
         * the av package also provides this network by default. However, this will change so in order to keep backward compatibility, we explicitly provide the
         * full
         * network here. Eventually Amodeus should be able to cope with what is defined by default. */

        return fullNetwork;
    }
}
