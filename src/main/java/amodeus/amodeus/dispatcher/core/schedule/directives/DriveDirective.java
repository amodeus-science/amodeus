package amodeus.amodeus.dispatcher.core.schedule.directives;

import org.matsim.api.core.v01.network.Link;

public interface DriveDirective extends Directive {
    Link getDestination();
}
