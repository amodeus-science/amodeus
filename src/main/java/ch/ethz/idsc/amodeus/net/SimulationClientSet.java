/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.concurrent.CopyOnWriteArraySet;

import ch.ethz.idsc.amodeus.util.net.ObjectHandler;

public class SimulationClientSet extends CopyOnWriteArraySet<ObjectHandler> {
    public static final SimulationClientSet INSTANCE = new SimulationClientSet();

    private SimulationClientSet() {
    }

}
