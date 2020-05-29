package org.matsim.amodeus.config.modal;

import org.matsim.amodeus.components.router.DefaultAVRouter;
import org.matsim.core.config.ReflectiveConfigGroup;

public class RouterConfig extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "router";

    static public final String TYPE = "type";

    static public final String DEFAULT_ROUTER = DefaultAVRouter.TYPE;
    private String type = DEFAULT_ROUTER;

    public RouterConfig() {
        super(GROUP_NAME, true);
    }

    @StringGetter(TYPE)
    public String getType() {
        return type;
    }

    @StringSetter(TYPE)
    public void setType(String type) {
        this.type = type;
    }
}
