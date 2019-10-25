/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

/* package */ class RebalanceVehicleEvent extends ActivityStartEvent {
    public static final String ACTTYPE = "AVRebalance";

    public static RebalanceVehicleEvent create(double time, RoboTaxi roboTaxi, Link link) {
        // get the id of the AV -related agent (driver) as id of vehicle not
        // possible to access directly
        Id<Person> id = new Id<Person>() {
            @Override
            public String toString() {
                return roboTaxi.getId().toString();
            }
        };
        return new RebalanceVehicleEvent(time, id, link.getId());
    }

    // ---
    private RebalanceVehicleEvent(double time, Id<Person> agentId, Id<Link> linkId) {
        super(time, agentId, linkId, null, ACTTYPE);
    }
}
