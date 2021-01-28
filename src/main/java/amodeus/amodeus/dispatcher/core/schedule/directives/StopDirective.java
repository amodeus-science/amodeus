package amodeus.amodeus.dispatcher.core.schedule.directives;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public interface StopDirective extends Directive {
    PassengerRequest getRequest();

    boolean isPickup();
}
