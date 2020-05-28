package ch.ethz.matsim.av.schedule;

import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.core.events.MatsimEventsReader.CustomEventMapper;

public class AmodeusRequestEventMapper implements CustomEventMapper<AmodeusRequestEvent> {
    @Override
    public AmodeusRequestEvent apply(GenericEvent event) {
        return AmodeusRequestEvent.fromGenericEvent(event);
    }
}
