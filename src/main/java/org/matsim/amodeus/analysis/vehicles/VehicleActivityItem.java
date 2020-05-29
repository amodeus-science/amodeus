package org.matsim.amodeus.analysis.vehicles;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class VehicleActivityItem {
    public String mode;

    public Id<Vehicle> vehicleId;
    public Link link;

    public double startTime = Double.NaN;
    public double endTime = Double.NaN;

    public String type;
}
