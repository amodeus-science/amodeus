package amodeus.amodeus.dispatcher.core.schedule.directives;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public class DefaultStopDirective extends AbstractDirective implements StopDirective {
    private final PassengerRequest request;
    private final boolean isPickup;

    public DefaultStopDirective(PassengerRequest request, boolean isPickup, boolean isModifiable) {
        super(isModifiable);

        this.request = request;
        this.isPickup = isPickup;
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
    public String toString() {
        return "SD[" + request.getId() + ", " + (isPickup ? "Pickup" : "Dropoff") + "]";
    }
}
