/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.HasLinkId;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

/* package */ class RebalanceVehicleEvent extends Event implements HasPersonId, HasLinkId {
    static public final String TYPE = "AVRebalance";

    private final Id<Person> personId;
    private final Id<Link> linkId;

    public static RebalanceVehicleEvent create(double time, RoboTaxi roboTaxi, Link link) {
        // get the id of the AV -related agent (driver) as id of vehicle not
        // possible to access directly
        return new RebalanceVehicleEvent(time, Id.createPersonId(roboTaxi.getId()), link.getId());
    }

    // ---
    private RebalanceVehicleEvent(double time, Id<Person> agentId, Id<Link> linkId) {
        super(time);

        this.personId = agentId;
        this.linkId = linkId;
    }

    @Override
    public String getEventType() {
        return TYPE;
    }

    @Override
    public Id<Person> getPersonId() {
        return personId;
    }

    @Override
    public Id<Link> getLinkId() {
        return linkId;
    }
}
