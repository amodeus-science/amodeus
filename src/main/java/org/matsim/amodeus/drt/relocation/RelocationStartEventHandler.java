package org.matsim.amodeus.drt.relocation;

import org.matsim.core.events.handler.EventHandler;

public interface RelocationStartEventHandler extends EventHandler {
    void handleEvent(RelocationStartEvent event);
}
