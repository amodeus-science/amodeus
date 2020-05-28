package ch.ethz.matsim.av.framework.registry;

import java.util.Map;

import ch.ethz.matsim.av.waiting_time.WaitingTimeFactory;

public class WaitingTimeRegistry extends NamedComponentRegistry<WaitingTimeFactory> {
    public WaitingTimeRegistry(Map<String, WaitingTimeFactory> components) {
        super("WaitingTime", components);
    }
}
