/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import java.util.Collections;

public class DummyStorageSupplier extends StorageSupplier {

    public DummyStorageSupplier() {
        super(Collections.emptyNavigableMap());
    }

    @Override
    public SimulationObject getSimulationObject(int index) throws Exception {
        return null;
    }

}
