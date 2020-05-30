package org.matsim.amodeus.framework.registry;

import java.util.Map;

import org.matsim.amodeus.components.AmodeusRouter;

public class RouterRegistry extends NamedComponentRegistry<AmodeusRouter.Factory> {
    public RouterRegistry(Map<String, AmodeusRouter.Factory> components) {
        super("AmodeusRouter", components);
    }
}
