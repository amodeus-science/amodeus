package org.matsim.amodeus.framework.registry;

import java.util.Map;

import org.matsim.amodeus.components.AVRouter;

public class RouterRegistry extends NamedComponentRegistry<AVRouter.Factory> {
    public RouterRegistry(Map<String, AVRouter.Factory> components) {
        super("AVRouter", components);
    }
}
