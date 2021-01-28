package org.matsim.amodeus.drt.relocation;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.events.HasLinkId;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

public class RelocationStartEvent extends Event implements HasLinkId {
    static public final String EVENT_TYPE = "drt relocation start";

    private final String mode;
    private final Id<DvrpVehicle> vehicleId;
    private final Id<Link> linkId;
    private final Id<Link> destinationLinkId;

    public RelocationStartEvent(double time, String mode, Id<DvrpVehicle> vehicleId, Id<Link> linkId, Id<Link> destinationLinkId) {
        super(time);

        this.mode = mode;
        this.vehicleId = vehicleId;
        this.linkId = linkId;
        this.destinationLinkId = destinationLinkId;
    }

    public Id<DvrpVehicle> getDvrpVehicleId() {
        return vehicleId;
    }

    @Override
    public Id<Link> getLinkId() {
        return linkId;
    }

    public Id<Link> getDestinationLinkId() {
        return destinationLinkId;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = super.getAttributes();
        attributes.put("mode", mode);
        attributes.put("vehicleId", vehicleId.toString());
        attributes.put("linkId", linkId.toString());
        attributes.put("destinationLinkId", destinationLinkId.toString());
        return attributes;
    }

    static RelocationStartEvent convert(GenericEvent event) {
        String mode = event.getAttributes().get("mode");
        Id<DvrpVehicle> vehicleId = Id.create(event.getAttributes().get("vehicleId"), DvrpVehicle.class);
        Id<Link> linkId = Id.create(event.getAttributes().get("linkId"), Link.class);
        Id<Link> destinationLinkId = Id.create(event.getAttributes().get("destinationLinkId"), Link.class);

        return new RelocationStartEvent(event.getTime(), mode, vehicleId, linkId, destinationLinkId);
    }
}
