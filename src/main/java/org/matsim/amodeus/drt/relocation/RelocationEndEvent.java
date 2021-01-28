package org.matsim.amodeus.drt.relocation;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.events.HasLinkId;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

public class RelocationEndEvent extends Event implements HasLinkId {
    static public final String EVENT_TYPE = "drt relocation end";

    private final String mode;
    private final Id<DvrpVehicle> vehicleId;
    private final Id<Link> linkId;

    public RelocationEndEvent(double time, String mode, Id<DvrpVehicle> vehicleId, Id<Link> linkId) {
        super(time);

        this.mode = mode;
        this.vehicleId = vehicleId;
        this.linkId = linkId;
    }

    public Id<DvrpVehicle> getDvrpVehicleId() {
        return vehicleId;
    }

    public Id<Link> getLinkId() {
        return linkId;
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
        return attributes;
    }

    static RelocationEndEvent convert(GenericEvent event) {
        String mode = event.getAttributes().get("mode");
        Id<DvrpVehicle> vehicleId = Id.create(event.getAttributes().get("vehicleId"), DvrpVehicle.class);
        Id<Link> linkId = Id.create(event.getAttributes().get("linkId"), Link.class);

        return new RelocationEndEvent(event.getTime(), mode, vehicleId, linkId);
    }
}
