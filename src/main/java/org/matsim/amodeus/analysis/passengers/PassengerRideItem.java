package org.matsim.amodeus.analysis.passengers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.vehicles.Vehicle;

public class PassengerRideItem {
    public String mode;

    public Id<Person> personId;
    public Id<Vehicle> vehicleId;

    public Link originLink;
    public Link destinationLink;

    public double departureTime = Double.NaN;
    public double arrivalTime = Double.NaN;
    public double waitingTime = Double.NaN;

    public double distance = 0.0;
}
