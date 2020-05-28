package org.matsim.amodeus.config.modal;

import org.matsim.amodeus.components.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import org.matsim.core.config.ReflectiveConfigGroup;

public class DispatcherConfig extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "dispatcher";

    static public final String TYPE = "type";

    static public final String DEFAULT_DISPATCHER = SingleHeuristicDispatcher.TYPE;
    private String type = DEFAULT_DISPATCHER;

    public DispatcherConfig() {
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
