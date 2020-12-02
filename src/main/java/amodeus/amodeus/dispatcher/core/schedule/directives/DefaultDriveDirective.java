package amodeus.amodeus.dispatcher.core.schedule.directives;

import org.matsim.api.core.v01.network.Link;

public class DefaultDriveDirective extends AbstractDirective implements DriveDirective {
    private final Link destination;

    public DefaultDriveDirective(Link destination, boolean isModifiable) {
        super(isModifiable);
        this.destination = destination;
    }

    @Override
    public Link getDestination() {
        return destination;
    }
}
