package ch.ethz.matsim.av.dispatcher;

import org.matsim.contrib.dvrp.run.ModalProviders;

import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;

public interface AVDispatcher {
    void onRequestSubmitted(AVRequest request);

    void onNextTaskStarted(AVVehicle vehicle);

    void onNextTimestep(double now);

    void addVehicle(AVVehicle vehicle);

    interface AVDispatcherFactory {
        AVDispatcher createDispatcher(ModalProviders.InstanceGetter inject);
    }
}
