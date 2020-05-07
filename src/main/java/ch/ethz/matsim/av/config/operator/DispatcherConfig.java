package ch.ethz.matsim.av.config.operator;

import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;

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
