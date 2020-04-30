package ch.ethz.matsim.av.schedule;

import org.matsim.core.events.handler.EventHandler;

public interface AVTransitEventHandler extends EventHandler {
	public void handleEvent(AVTransitEvent event);
}
