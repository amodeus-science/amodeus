/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import ch.ethz.idsc.amodeus.net.simobj.SimulationObject;
import ch.ethz.idsc.amodeus.util.net.ObjectHandler;

public enum SimulationDistribution {
    ;
    // ---

    public static void of(SimulationObject simulationObject, StorageUtils storageUtils) {
        SimulationObjects.sortVehiclesAccordingToIndex(simulationObject);

        new StorageSubscriber(storageUtils).handle(simulationObject);

        if (SimulationServer.INSTANCE.getWaitForClients()) { // <- server is
                                                             // running &&
                                                             // wait for
                                                             // clients is
                                                             // set
            if (SimulationClientSet.INSTANCE.isEmpty())
                System.out.println("waiting for connections...");
            // block for connections
            while (SimulationClientSet.INSTANCE.isEmpty())
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        for (ObjectHandler objectHandler : SimulationClientSet.INSTANCE)
            objectHandler.handle(simulationObject);
    }
}
