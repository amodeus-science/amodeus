package ch.ethz.matsim.av.dispatcher;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

public interface AVDispatcher {
	void onRequestSubmitted(AVRequest request);

	void onNextTaskStarted(AVVehicle vehicle);

	void onNextTimestep(double now);

	void addVehicle(AVVehicle vehicle);

	interface AVDispatcherFactory {
		AVDispatcher createDispatcher(OperatorConfig operatorConfig, AVRouter router, Network network);
	}
}
