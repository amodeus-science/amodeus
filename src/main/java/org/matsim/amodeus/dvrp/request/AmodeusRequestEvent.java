package org.matsim.amodeus.dvrp.request;

import java.util.Map;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;
import org.matsim.core.utils.misc.OptionalTime;

public class AmodeusRequestEvent extends Event implements HasPersonId {
    static public final String TYPE = "AVTransit";

    final private Id<Person> personId;
    final private String mode;

    final private Optional<Double> expectedDistance;
    final private OptionalTime expectedTravelTime;
    final private Optional<Double> expectedPrice;
    final private OptionalTime expectedWaitingTime;

    private AmodeusRequestEvent(double time, Id<Person> personId, String mode, Optional<Double> expectedDistance, OptionalTime expectedTravelTime, Optional<Double> expectedPrice,
            OptionalTime expectedWaitingTime) {
        super(time);

        this.personId = personId;
        this.mode = mode;

        this.expectedDistance = expectedDistance;
        this.expectedTravelTime = expectedTravelTime;
        this.expectedPrice = expectedPrice;
        this.expectedWaitingTime = expectedWaitingTime;
    }

    static public AmodeusRequestEvent fromRequest(double time, AmodeusRequest request) {
        return new AmodeusRequestEvent( //
                time, //
                request.getPassengerId(), //
                request.getMode(), //
                request.getRoute().getExpectedDistance(), //
                request.getRoute().getTravelTime(), //
                request.getRoute().getPrice(), //
                request.getRoute().getWaitingTime() //
        );
    }

    static AmodeusRequestEvent fromGenericEvent(GenericEvent event) {
        Optional<Double> expectedTravelTime = Optional.ofNullable(event.getAttributes().get("expectedTravelTime")).map(Double::parseDouble);
        Optional<Double> expectedWaitingTime = Optional.ofNullable(event.getAttributes().get("expectedWaitingTime")).map(Double::parseDouble);
        Optional<Double> expectedPrice = Optional.ofNullable(event.getAttributes().get("expectedPrice")).map(Double::parseDouble);
        Optional<Double> expectedDistance = Optional.ofNullable(event.getAttributes().get("expectedDistance")).map(Double::parseDouble);

        return new AmodeusRequestEvent( //
                event.getTime(), //
                Id.createPersonId(event.getAttributes().get("person")), //
                event.getAttributes().get("mode"), //
                expectedDistance, //
                expectedTravelTime.map(OptionalTime::defined).orElse(OptionalTime.undefined()), //
                expectedPrice, //
                expectedWaitingTime.map(OptionalTime::defined).orElse(OptionalTime.undefined()) //
        );
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put("person", personId.toString());
        attr.put("mode", mode.toString());

        if (expectedDistance.isPresent()) {
            attr.put("expectedDistance", String.valueOf(expectedDistance));
        }

        if (expectedTravelTime.isDefined()) {
            attr.put("expectedTravelTime", String.valueOf(expectedTravelTime));
        }

        if (expectedPrice.isPresent()) {
            attr.put("expectedPrice", String.valueOf(expectedPrice));
        }

        if (expectedWaitingTime.isDefined()) {
            attr.put("expectedWaitingTime", String.valueOf(expectedWaitingTime));
        }

        return attr;
    }

    @Override
    public Id<Person> getPersonId() {
        return personId;
    }

    public String getMode() {
        return mode;
    }

    public Optional<Double> getExpectedDistance() {
        return expectedDistance;
    }

    public OptionalTime getExpectedTravelTime() {
        return expectedTravelTime;
    }

    public Optional<Double> getExpectedPrice() {
        return expectedPrice;
    }

    public OptionalTime getExpectedWaitingTime() {
        return expectedWaitingTime;
    }

    @Override
    public String getEventType() {
        return TYPE;
    }
}
