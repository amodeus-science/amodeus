package amodeus.amodeus.dispatcher.core.schedule;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public class DefaultStop implements Stop {
    private final PassengerRequest request;
    private final boolean isPickup;
    private final boolean isModifiable;

    public DefaultStop(PassengerRequest request, boolean isPickup, boolean isModifiable) {
        this.request = request;
        this.isPickup = isPickup;
        this.isModifiable = isModifiable;
    }

    @Override
    public PassengerRequest getRequest() {
        return request;
    }

    @Override
    public boolean isPickup() {
        return isPickup;
    }

    @Override
    public boolean isModifiable() {
        return isModifiable;
    }

}
