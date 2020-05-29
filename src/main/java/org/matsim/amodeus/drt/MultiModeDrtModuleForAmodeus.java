package org.matsim.amodeus.drt;

import org.matsim.contrib.drt.routing.MultiModeDrtMainModeIdentifier;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtModeModule;
import org.matsim.contrib.drt.run.DrtModeQSimModule;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.MainModeIdentifier;

import com.google.inject.Inject;

public class MultiModeDrtModuleForAmodeus extends AbstractModule {

    @Inject
    private MultiModeDrtConfigGroup multiModeDrtCfg;

    @Override
    public void install() {
        for (DrtConfigGroup drtCfg : multiModeDrtCfg.getModalElements()) {
            install(new DrtModeModule(drtCfg));
            installQSimModule(new DrtModeQSimModule(drtCfg));
            // install(new DrtModeAnalysisModule(drtCfg));
        }

        bind(MainModeIdentifier.class).toInstance(new MultiModeDrtMainModeIdentifier(multiModeDrtCfg));
    }
}