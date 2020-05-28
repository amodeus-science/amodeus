package org.matsim.amodeus.framework.registry;

import java.util.Map;

import org.matsim.amodeus.components.AVDispatcher;

public class DispatcherRegistry extends NamedComponentRegistry<AVDispatcher.AVDispatcherFactory> {
    public DispatcherRegistry(Map<String, AVDispatcher.AVDispatcherFactory> components) {
        super("AVDispatcher", components);
    }
}
