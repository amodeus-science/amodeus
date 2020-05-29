/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.amodeus.framework.AVQSimModule;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

public class AmodeusQSimModule extends AbstractQSimModule {
    @Override
    protected void configureQSim() {
        /* Basically, we install the QSimModule that is provided by the AV package, but
         * we override one little detail: Instead of the standard LegCreator a custom
         * one is used which adds IDSC tracking functionality to the dynamic vehicle
         * legs. */

        install(new AVQSimModule());
    }
}
