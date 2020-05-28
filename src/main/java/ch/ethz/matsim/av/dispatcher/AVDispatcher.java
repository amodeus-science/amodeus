package ch.ethz.matsim.av.dispatcher;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.run.ModalProviders;

import ch.ethz.matsim.av.passenger.AVRequest;

public interface AVDispatcher {
    void onRequestSubmitted(AVRequest request);

    void onNextTaskStarted(DvrpVehicle vehicle);

    void onNextTimestep(double now);

    void addVehicle(DvrpVehicle vehicle);

    interface AVDispatcherFactory {
        AVDispatcher createDispatcher(ModalProviders.InstanceGetter inject);
    }
}
