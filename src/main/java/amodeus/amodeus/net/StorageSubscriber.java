/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import java.io.File;
import java.util.Objects;

import amodeus.amodeus.util.net.ObjectHandler;
import ch.ethz.idsc.tensor.io.Export;

public class StorageSubscriber implements ObjectHandler {

    private final StorageUtils storageUtils;

    public StorageSubscriber(StorageUtils storageUtils) {
        this.storageUtils = Objects.requireNonNull(storageUtils);
    }

    @Override
    public void handle(Object object) {
        File file = null;
        try {
            SimulationObject simulationObject = (SimulationObject) object;
            file = storageUtils.getFileForStorageOf(simulationObject);
            Export.object(file, simulationObject);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException(file.getAbsolutePath());
        }
    }
}
