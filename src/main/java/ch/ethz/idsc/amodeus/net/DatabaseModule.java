/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import org.matsim.core.controler.AbstractModule;

/** tracks iteration count which is required to store SimulationObjects
 * of different iterations for offline analysis. */
public class DatabaseModule extends AbstractModule {

    @Override
    public void install() {
        addControlerListenerBinding().to(DatabaseIterationStartsListener.class);
        addControlerListenerBinding().to(DatabaseIterationEndsListener.class);
    }
}
