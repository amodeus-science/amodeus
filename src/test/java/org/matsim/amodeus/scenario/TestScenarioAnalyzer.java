package org.matsim.amodeus.scenario;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.routing.AmodeusRoutingModule;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.core.controler.AbstractModule;

public class TestScenarioAnalyzer extends AbstractModule
        implements PersonDepartureEventHandler, PersonArrivalEventHandler, ActivityStartEventHandler {
    public long numberOfDepartures;
    public long numberOfArrivals;
    public long numberOfInteractionActivities;

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (event.getLegMode().equals(AmodeusModeConfig.DEFAULT_MODE)) {
            numberOfArrivals++;
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(AmodeusModeConfig.DEFAULT_MODE)) {
            numberOfDepartures++;
        }
    }

    @Override
    public void reset(int iteration) {
    }

    @Override
    public void install() {
        addEventHandlerBinding().toInstance(this);
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().equals(AmodeusRoutingModule.INTERACTION_ACTIVITY_TYPE)) {
            numberOfInteractionActivities++;
        }
    }
}
