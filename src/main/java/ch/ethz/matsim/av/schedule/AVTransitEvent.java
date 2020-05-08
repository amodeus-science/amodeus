package ch.ethz.matsim.av.schedule;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AVTransitEvent extends Event implements HasPersonId {
    static public final String TYPE = "AVTransit";

    final private AVRequest request;

    final private Id<Person> personId;
    final private String mode;
    final private double distance;
    final private double price;

    public AVTransitEvent(AVRequest request, double time) {
        this(request.getPassengerId(), request.getMode(), request.getRoute().getDistance(), request.getRoute().getPrice(), time, request);
    }

    public AVTransitEvent(Id<Person> personId, String mode, double distance, double price, double time) {
        this(personId, mode, distance, price, time, null);
    }

    private AVTransitEvent(Id<Person> personId, String mode, double distance, double price, double time, AVRequest request) {
        super(time);

        this.request = request;
        this.personId = personId;
        this.mode = mode;
        this.distance = distance;
        this.price = price;
    }

    public AVRequest getRequest() {
        if (request == null) {
            throw new IllegalStateException();
        }

        return request;
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put("person", personId.toString());
        attr.put("mode", mode.toString());
        attr.put("distance", String.valueOf(distance));

        if (!Double.isNaN(price)) {
            attr.put("price", String.valueOf(price));
        }

        return attr;
    }

    @Override
    public Id<Person> getPersonId() {
        return personId;
    }

    public double getDistance() {
        return distance;
    }

    public String getMode() {
        return mode;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String getEventType() {
        return TYPE;
    }
}
