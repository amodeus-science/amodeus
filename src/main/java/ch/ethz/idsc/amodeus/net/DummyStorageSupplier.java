/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Collections;

import ch.ethz.idsc.amodeus.net.simobj.SimulationObject;

public class DummyStorageSupplier extends StorageSupplier {

    public DummyStorageSupplier() {
        super(Collections.emptyNavigableMap());
    }

    @Override
    public SimulationObject getSimulationObject(int index) throws Exception {
        return null;
    }

}
