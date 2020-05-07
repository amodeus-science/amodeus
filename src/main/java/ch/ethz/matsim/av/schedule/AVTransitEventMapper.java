package ch.ethz.matsim.av.schedule;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.events.MatsimEventsReader.CustomEventMapper;

import ch.ethz.matsim.av.data.AVOperator;

public class AVTransitEventMapper implements CustomEventMapper<AVTransitEvent> {
    @Override
    public AVTransitEvent apply(GenericEvent event) {
        Id<Person> personId = Id.create(event.getAttributes().get("person"), Person.class);
        Id<AVOperator> operatorId = Id.create(event.getAttributes().get("operator"), AVOperator.class);
        double distance = Double.parseDouble(event.getAttributes().get("distance"));
        double price = Double.parseDouble(event.getAttributes().getOrDefault("price", "NaN"));
        return new AVTransitEvent(personId, operatorId, distance, price, event.getTime());
    }
}
