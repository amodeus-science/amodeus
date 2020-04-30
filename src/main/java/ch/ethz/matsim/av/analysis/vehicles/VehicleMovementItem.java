package ch.ethz.matsim.av.analysis.vehicles;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

import ch.ethz.matsim.av.data.AVOperator;

public class VehicleMovementItem {
	public Id<AVOperator> operatorId;
	public Id<Vehicle> vehicleId;

	public Link originLink;
	public Link destinationLink;

	public double departureTime = Double.NaN;
	public double arrivalTime = Double.NaN;

	public double distance = 0;
	public int numberOfPassengers = 0;
}
