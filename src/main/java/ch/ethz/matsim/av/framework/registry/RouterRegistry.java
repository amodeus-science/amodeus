package ch.ethz.matsim.av.framework.registry;

import java.util.Map;

import ch.ethz.matsim.av.router.AVRouter;

public class RouterRegistry extends NamedComponentRegistry<AVRouter.Factory> {
    public RouterRegistry(Map<String, AVRouter.Factory> components) {
        super("AVRouter", components);
    }
}
