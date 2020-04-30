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

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.generator.AVUtils;

@Singleton
public class WaitingTimeListener implements PersonDepartureEventHandler, PersonEntersVehicleEventHandler, AfterMobsimListener {
	private final Map<Id<AVOperator>, WaitingTimeCollector> collectors;
	private final Map<Id<Person>, PersonDepartureEvent> departureEvents = new HashMap<>();

	@Inject
	public WaitingTimeListener(Map<Id<AVOperator>, WaitingTimeCollector> collectors) {
		this.collectors = collectors;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getLegMode().equals(AVModule.AV_MODE)) {
			departureEvents.put(event.getPersonId(), event);
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent enterEvent) {
		if (enterEvent.getVehicleId().toString().startsWith("av:")) {
			if (!enterEvent.getPersonId().toString().startsWith("av:")) {
				Id<AVOperator> operatorId = AVUtils.getOperatorId(enterEvent.getVehicleId());
				WaitingTimeCollector collector = collectors.get(operatorId);

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
		for (WaitingTimeCollector collector : collectors.values()) {
			collector.consolidate();
		}

		departureEvents.clear();
	}
}
