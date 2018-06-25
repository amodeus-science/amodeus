/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

/* package */ class RebalanceVehicleEvent extends ActivityStartEvent {
    public static final String ACTTYPE = "AVRebalance";

    private RebalanceVehicleEvent( //
            final double time, //
            final Id<Person> agentId, //
            final Id<Link> linkId) {
        super(time, agentId, linkId, null, ACTTYPE);
    }

    public static RebalanceVehicleEvent create(final double time, //
            final UnitCapRoboTaxi robotaxi, //
            final Link link) {

        // get the id of the AV -related agent (driver) as id of vehilce not
        // possible to access directly
        Id<Person> id = new Id<Person>() {
            @Override
            public String toString() {
                return robotaxi.getId().toString();
            }
        };

        return new RebalanceVehicleEvent(time, id, link.getId());

    }

}
