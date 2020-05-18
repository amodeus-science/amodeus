package ch.ethz.matsim.av.schedule;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.events.MatsimEventsReader.CustomEventMapper;

public class AVTransitEventMapper implements CustomEventMapper<AVTransitEvent> {
    @Override
    public AVTransitEvent apply(GenericEvent event) {
        Id<Person> personId = Id.create(event.getAttributes().get("person"), Person.class);
        String mode = (String) event.getAttributes().get("mode");
        double distance = Double.parseDouble(event.getAttributes().get("distance"));
        double price = Double.parseDouble(event.getAttributes().getOrDefault("price", "NaN"));
        return new AVTransitEvent(personId, mode, distance, price, event.getTime());
    }
}
