package ch.ethz.idsc.amodeus.analysis.traces;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class AVTraceItem {
	public Id<Vehicle> vehicleId;
	public String vehicleType;

	public Link originLink;
	public Link destinationLink;
	
	public int originZone = -1;
	public int destinationZone = -1;

	public double departureTime;
	public double arrivalTime;

	public double distance = 0.0;
	public int occupancy = 0;

	public String followingTaskType;
	public double followingTaskDuration;
}
