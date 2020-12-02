package amodeus.amodeus.dispatcher.core.schedule.directives;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public interface Directive {
    boolean isModifiable();

    static public StopDirective pickup(PassengerRequest request) {
        return new DefaultStopDirective(request, true, true);
    }

    static public StopDirective dropoff(PassengerRequest request) {
        return new DefaultStopDirective(request, false, true);
    }

    static public DriveDirective drive(Link link) {
        return new DefaultDriveDirective(link, true);
    }

    static public Link getLink(Directive directive) {
        if (directive instanceof StopDirective) {
            StopDirective stopDirective = (StopDirective) directive;

            if (stopDirective.isPickup()) {
                return stopDirective.getRequest().getFromLink();
            } else {
                return stopDirective.getRequest().getToLink();
            }
        } else {
            return ((DriveDirective) directive).getDestination();
        }
    }
}
