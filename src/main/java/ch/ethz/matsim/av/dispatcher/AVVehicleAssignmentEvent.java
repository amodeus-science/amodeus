package ch.ethz.matsim.av.dispatcher;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

public class AVVehicleAssignmentEvent extends Event {
    private final Id<DvrpVehicle> vehicleId;
    private final String mode;

    public AVVehicleAssignmentEvent(String mode, Id<DvrpVehicle> vehicleId, double time) {
        super(time);

        this.vehicleId = vehicleId;
        this.mode = mode;
    }

    @Override
    public String getEventType() {
        return "AVVehicleAssignment";
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put("vehicle", vehicleId.toString());
        attr.put("mode", mode);
        return attr;
    }
}
