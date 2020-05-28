package org.matsim.amodeus.dvrp.request;

import org.matsim.core.events.handler.EventHandler;

public interface AmodeusRequestEventHandler extends EventHandler {
    public void handleEvent(AmodeusRequestEvent event);
}
