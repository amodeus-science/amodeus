/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import java.util.concurrent.CopyOnWriteArraySet;

import amodeus.amodeus.util.net.ObjectHandler;

/* package */ class SimulationClientSet extends CopyOnWriteArraySet<ObjectHandler> {
    public static final SimulationClientSet INSTANCE = new SimulationClientSet();

    private SimulationClientSet() {
        // ---
    }
}
