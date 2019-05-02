/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.service;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.av.data.AVOperator;

public class AVServiceItem {
    public Id<Person> personId;
    public int tripIndex;

    public Link originLink;
    public Link destinationLink;

    public double departureTime;

    public double waitingTime;
    public double inVehicleTime;

    public double distance = 0.0;
    public double chargedDistance = 0.0;

    public Id<AVOperator> operatorId;
}
