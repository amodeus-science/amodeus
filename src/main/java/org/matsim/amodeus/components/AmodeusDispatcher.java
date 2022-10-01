package org.matsim.amodeus.components;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.modal.ModalProviders;

public interface AmodeusDispatcher {
    void onRequestSubmitted(PassengerRequest request);

    void onNextTaskStarted(DvrpVehicle vehicle);

    void onNextTimestep(double now);

    void addVehicle(DvrpVehicle vehicle);

    interface AVDispatcherFactory {
        AmodeusDispatcher createDispatcher(ModalProviders.InstanceGetter inject);
    }
}
