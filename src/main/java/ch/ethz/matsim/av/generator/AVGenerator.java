package ch.ethz.matsim.av.generator;

import java.util.List;

import org.matsim.api.core.v01.network.Network;
import org.matsim.vehicles.VehicleType;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;

public interface AVGenerator {
	List<AVVehicle> generateVehicles();

	interface AVGeneratorFactory {
		AVGenerator createGenerator(OperatorConfig operatorConfig, Network network, VehicleType vehicleType);
	}
}
