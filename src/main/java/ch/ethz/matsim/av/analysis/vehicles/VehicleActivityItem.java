package ch.ethz.matsim.av.analysis.vehicles;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

import ch.ethz.matsim.av.data.AVOperator;

public class VehicleActivityItem {
	public Id<AVOperator> operatorId;
	public Id<Vehicle> vehicleId;

	public Link link;

	public double startTime = Double.NaN;
	public double endTime = Double.NaN;

	public String type;
}
