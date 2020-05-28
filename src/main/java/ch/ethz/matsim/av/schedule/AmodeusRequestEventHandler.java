package ch.ethz.matsim.av.schedule;

import org.matsim.core.events.handler.EventHandler;

public interface AmodeusRequestEventHandler extends EventHandler {
    public void handleEvent(AmodeusRequestEvent event);
}
