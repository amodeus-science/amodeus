package ch.ethz.matsim.av.config.operator;

import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.routing.interaction.ClosestLinkInteractionFinder;

public class InteractionFinderConfig extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "interactionFinder";

    static public final String TYPE = "type";

    static public final String DEFAULT_INTERACTION_FINDER = ClosestLinkInteractionFinder.TYPE;
    private String type = DEFAULT_INTERACTION_FINDER;

    public InteractionFinderConfig() {
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
