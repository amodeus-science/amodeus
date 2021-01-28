package org.matsim.amodeus.dvrp.passenger;

import org.matsim.core.events.handler.EventHandler;

public interface PassengerRequestUnscheduledEventHandler extends EventHandler {
    void handleEvent(final PassengerRequestUnscheduledEvent event);
}