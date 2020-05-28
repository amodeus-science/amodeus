package org.matsim.amodeus.framework.registry;

import java.util.Map;

import org.matsim.amodeus.waiting_time.WaitingTimeFactory;

public class WaitingTimeRegistry extends NamedComponentRegistry<WaitingTimeFactory> {
    public WaitingTimeRegistry(Map<String, WaitingTimeFactory> components) {
        super("WaitingTime", components);
    }
}
