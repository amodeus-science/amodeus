/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.service;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.av.data.AVOperator;

public class AVServiceItem {
    Id<Person> personId;
    int tripIndex;

    Link originLink;
    Link destinationLink;
    double departureTime;

    double waitingTime;
    double inVehicleTime;

    double distance = 0.0;
    double chargedDistance = 0.0;

    Id<AVOperator> operatorId;
}
