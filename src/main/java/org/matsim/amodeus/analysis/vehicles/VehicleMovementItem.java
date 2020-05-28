package org.matsim.amodeus.analysis.vehicles;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class VehicleMovementItem {
    public String mode;
    public Id<Vehicle> vehicleId;

    public Link originLink;
    public Link destinationLink;

    public double departureTime = Double.NaN;
    public double arrivalTime = Double.NaN;

    public double distance = 0;
    public int numberOfPassengers = 0;
}
