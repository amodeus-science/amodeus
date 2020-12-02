package amodeus.amodeus.dispatcher.core.schedule;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public interface Stop {
    PassengerRequest getRequest();

    boolean isPickup();

    boolean isModifiable();
}
