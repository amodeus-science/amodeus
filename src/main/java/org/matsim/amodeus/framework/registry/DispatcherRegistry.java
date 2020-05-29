package org.matsim.amodeus.framework.registry;

import java.util.Map;

import org.matsim.amodeus.components.AmodeusDispatcher;

public class DispatcherRegistry extends NamedComponentRegistry<AmodeusDispatcher.AVDispatcherFactory> {
    public DispatcherRegistry(Map<String, AmodeusDispatcher.AVDispatcherFactory> components) {
        super("AVDispatcher", components);
    }
}
