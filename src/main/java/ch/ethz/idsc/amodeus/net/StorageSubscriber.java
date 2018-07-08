/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.io.File;
import java.util.Objects;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.net.ObjectHandler;
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
            System.err.println(file.getAbsolutePath());
            exception.printStackTrace();
            GlobalAssert.that(false);
        }
    }
}
