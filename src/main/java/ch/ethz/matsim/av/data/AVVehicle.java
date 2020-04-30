package ch.ethz.matsim.av.data;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleImpl;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.vehicles.VehicleType;

import ch.ethz.matsim.av.dispatcher.AVDispatcher;

public class AVVehicle extends DvrpVehicleImpl {
	private AVOperator operator = null;
	private AVDispatcher dispatcher;
	private VehicleType vehicleType;

	public AVVehicle(Id<DvrpVehicle> id, Link startLink, double t0, double t1, VehicleType vehicleType) {
		super(ImmutableDvrpVehicleSpecification.newBuilder().id(id).capacity(vehicleType.getCapacity().getSeats())
				.startLinkId(startLink.getId()).serviceBeginTime(t0).serviceEndTime(t1).build(), startLink);
		this.vehicleType = vehicleType;
	}

	public AVOperator getOperator() {
		return operator;
	}

	public AVDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setOperator(AVOperator operator) {
		this.operator = operator;
	}

	public void setDispatcher(AVDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}
}
