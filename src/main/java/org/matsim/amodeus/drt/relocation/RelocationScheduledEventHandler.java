package org.matsim.amodeus.drt.relocation;

import org.matsim.core.events.handler.EventHandler;

public interface RelocationScheduledEventHandler extends EventHandler {
    void handleEvent(RelocationScheduledEvent event);
}
