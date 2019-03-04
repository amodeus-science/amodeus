/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.QSim;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;

import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.schedule.AVOptimizer;

public class AmodeusQSimModule extends AbstractQSimModule {
    @Override
    protected void configureQSim() {
        /* Basically, we install the QSimModule that is provided by the AV package, but
         * we override one little detail: Instead of the standard LegCreator a custom
         * one is used which adds IDSC tracking functionality to the dynamic vehicle
         * legs. */

        install(Modules.override(new AVQSimModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                // ---
            }

            @Provides
            @Singleton
            VrpLegFactory provideLegCreator(AVOptimizer avOptimizer, QSim qsim) {
                return TrackingHelper.createLegCreatorWithIDSCTracking(avOptimizer, qsim.getSimTimer());
            }
        }));
    }
}
