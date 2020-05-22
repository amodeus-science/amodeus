package ch.ethz.matsim.av.waiting_time;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.generator.AmodeusIdentifiers;

@Singleton
public class WaitingTimeListener implements PersonDepartureEventHandler, PersonEntersVehicleEventHandler, AfterMobsimListener {
    private final WaitingTimeCollector collector;
    private final String mode;

    private final Map<Id<Person>, PersonDepartureEvent> departureEvents = new HashMap<>();

    @Inject
    public WaitingTimeListener(WaitingTimeCollector collector, String mode) {
        this.collector = collector;
        this.mode = mode;
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(mode)) {
            departureEvents.put(event.getPersonId(), event);
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent enterEvent) {
        if (AmodeusIdentifiers.isValid(enterEvent.getVehicleId())) {
            if (!AmodeusIdentifiers.isValid(enterEvent.getPersonId())) {
                if (collector != null) {
                    PersonDepartureEvent departureEvent = departureEvents.remove(enterEvent.getPersonId());

                    if (departureEvent != null) {
                        double waitingTime = enterEvent.getTime() - departureEvent.getTime();
                        Id<Link> linkId = departureEvent.getLinkId();

                        collector.registerWaitingTime(departureEvent.getTime(), waitingTime, linkId);
                    }
                }
            }
        }
    }

    @Override
    public void reset(int iteration) {

    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {
        collector.consolidate();
        departureEvents.clear();
    }
}
