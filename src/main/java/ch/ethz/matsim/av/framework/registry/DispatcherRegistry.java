package ch.ethz.matsim.av.framework.registry;

import java.util.Map;

import ch.ethz.matsim.av.dispatcher.AVDispatcher;

public class DispatcherRegistry extends NamedComponentRegistry<AVDispatcher.AVDispatcherFactory> {
    public DispatcherRegistry(Map<String, AVDispatcher.AVDispatcherFactory> components) {
        super("AVDispatcher", components);
    }
}
