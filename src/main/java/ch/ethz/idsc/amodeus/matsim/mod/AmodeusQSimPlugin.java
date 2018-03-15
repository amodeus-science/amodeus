/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Collection;
import java.util.Collections;

import org.matsim.core.config.Config;

import com.google.inject.Module;

import ch.ethz.matsim.av.framework.AVQSimPlugin;

public class AmodeusQSimPlugin extends AVQSimPlugin {
    public AmodeusQSimPlugin(Config config) {
        super(config);
    }

    @Override
    public Collection<? extends Module> modules() {
        /* Here we override the AVQSimPlugin from the AV package and instead of
         * providing the AVQSimModule we provide the AmodeusQSimModule, which makes sure
         * that specific components for Amodeus (IDSC vehicle tracking) can be injected. */
        return Collections.singleton(new AmodeusQSimModule());
    }
}
