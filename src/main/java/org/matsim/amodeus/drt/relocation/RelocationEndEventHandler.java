package org.matsim.amodeus.drt.relocation;

import org.matsim.core.events.handler.EventHandler;

public interface RelocationEndEventHandler extends EventHandler {
    void handleEvent(RelocationEndEvent event);
}
