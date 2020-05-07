package ch.ethz.matsim.av.schedule;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.passenger.AVRequest;

public class AVTransitEvent extends Event implements HasPersonId {
    static public final String TYPE = "AVTransit";

    final private AVRequest request;

    final private Id<Person> personId;
    final private Id<AVOperator> operatorId;
    final private double distance;
    final private double price;

    public AVTransitEvent(AVRequest request, double time) {
        this(request.getPassengerId(), request.getOperatorId(), request.getRoute().getDistance(), Double.NaN, time, request);
    }

    public AVTransitEvent(Id<Person> personId, Id<AVOperator> operatorId, double distance, double price, double time) {
        this(personId, operatorId, distance, price, time, null);
    }

    private AVTransitEvent(Id<Person> personId, Id<AVOperator> operatorId, double distance, double price, double time, AVRequest request) {
        super(time);

        this.request = request;
        this.personId = personId;
        this.operatorId = operatorId;
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
        attr.put("operator", operatorId.toString());
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

    public Id<AVOperator> getOperatorId() {
        return operatorId;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String getEventType() {
        return TYPE;
    }
}
