package org.matsim.amodeus.analysis;

import org.matsim.amodeus.components.generator.AmodeusIdentifiers;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

public class FleetInformationListener implements BeforeMobsimListener, ActivityStartEventHandler, PersonDepartureEventHandler, PersonEntersVehicleEventHandler,
        PersonLeavesVehicleEventHandler, LinkEnterEventHandler {
    private final LinkFinder linkFinder;
    private final PassengerTracker passengers = new PassengerTracker();
    private final String mode;

    private FleetInformation data;

    static public class FleetInformation {
        public int numberOfRequests;
        public int numberOfVehicles;

        public double vehicleDistance_m;
        public double emptyDistance_m;
        public double passengerDistance_m;
    }

    public FleetInformationListener(String mode, LinkFinder linkFinder) {
        this.linkFinder = linkFinder;
        this.mode = mode;
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(mode)) {
            data.numberOfRequests++;
        }
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (AmodeusIdentifiers.isValid(event.getPersonId())) {
            String vehicleMode = AmodeusIdentifiers.getMode(event.getPersonId());

            if (mode.equals(vehicleMode)) {
                data.numberOfVehicles++;
            }
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
                String vehicleMode = AmodeusIdentifiers.getMode(event.getVehicleId());

                if (mode.equals(vehicleMode)) {
                    passengers.addPassenger(event.getVehicleId(), event.getPersonId());
                }
            }
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
                String vehicleMode = AmodeusIdentifiers.getMode(event.getVehicleId());

                if (mode.equals(vehicleMode)) {
                    passengers.removePassenger(event.getVehicleId(), event.getPersonId());
                }
            }
        }
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
            String vehicleMode = AmodeusIdentifiers.getMode(event.getVehicleId());

            if (mode.equals(vehicleMode)) {
                double linkLength = linkFinder.getDistance(event.getLinkId());
                int numberOfPassengers = passengers.getNumberOfPassengers(event.getVehicleId());

                if (numberOfPassengers == 0) {
                    data.emptyDistance_m += linkLength;
                }

                data.vehicleDistance_m += linkLength;
                data.passengerDistance_m += linkLength * numberOfPassengers;
            }
        }
    }

    public FleetInformation getInformation() {
        return data;
    }

    @Override
    public void notifyBeforeMobsim(BeforeMobsimEvent event) {
        // We do it like that, because in some cases (no prediction of prices) the FleetDistanceListener is not even requested via injection. In such cases, it will not
        // capture events.
        event.getServices().getEvents().addHandler(this);

        passengers.clear();
        data = new FleetInformation();
    }
}
