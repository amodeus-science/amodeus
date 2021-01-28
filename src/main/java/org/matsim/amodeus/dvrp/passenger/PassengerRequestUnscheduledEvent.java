package org.matsim.amodeus.dvrp.passenger;

import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.core.api.internal.HasPersonId;

// TODO: Cannot derive from AbstractPassengerRequestEvent because constructor is not public
public class PassengerRequestUnscheduledEvent extends Event implements HasPersonId {
    public static final String EVENT_TYPE = "PassengerRequest unscheduled";

    public static final String ATTRIBUTE_MODE = "mode";
    public static final String ATTRIBUTE_REQUEST = "request";
    public static final String ATTRIBUTE_VEHICLE = "vehicle";

    private final String mode;
    private final Id<Request> requestId;
    private final Id<Person> personId;
    private final Id<DvrpVehicle> vehicleId;

    public PassengerRequestUnscheduledEvent(double time, String mode, Id<Request> requestId, Id<Person> personId, Id<DvrpVehicle> vehicleId) {
        super(time);
        this.mode = mode;
        this.requestId = requestId;
        this.personId = personId;
        this.vehicleId = vehicleId;
    }

    public final String getMode() {
        return mode;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    /** @return id of the vehicle assigned to the request */
    public final Id<DvrpVehicle> getVehicleId() {
        return vehicleId;
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = getSuperAttributes();
        attr.put(ATTRIBUTE_VEHICLE, vehicleId + "");
        return attr;
    }

    /** @return id of the request */
    public final Id<Request> getRequestId() {
        return requestId;
    }

    /** @return id of the passenger (person) */
    @Override
    public final Id<Person> getPersonId() {
        return personId;
    }

    private Map<String, String> getSuperAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put(ATTRIBUTE_MODE, mode);
        attr.put(ATTRIBUTE_REQUEST, requestId + "");
        return attr;
    }

    public static PassengerRequestUnscheduledEvent convert(GenericEvent event) {
        Map<String, String> attributes = event.getAttributes();
        double time = Double.parseDouble(attributes.get(ATTRIBUTE_TIME));
        String mode = Objects.requireNonNull(attributes.get(ATTRIBUTE_MODE));
        Id<Request> requestId = Id.create(attributes.get(ATTRIBUTE_REQUEST), Request.class);
        Id<Person> personId = Id.createPersonId(attributes.get(ATTRIBUTE_PERSON));
        Id<DvrpVehicle> vehicleId = Id.create(attributes.get(ATTRIBUTE_VEHICLE), DvrpVehicle.class);
        return new PassengerRequestUnscheduledEvent(time, mode, requestId, personId, vehicleId);
    }
}
