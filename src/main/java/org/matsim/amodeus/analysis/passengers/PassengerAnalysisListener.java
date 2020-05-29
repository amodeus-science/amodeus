package org.matsim.amodeus.analysis.passengers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.amodeus.analysis.LinkFinder;
import org.matsim.amodeus.analysis.PassengerTracker;
import org.matsim.amodeus.components.generator.AmodeusIdentifiers;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.population.Person;

public class PassengerAnalysisListener
        implements PersonDepartureEventHandler, PersonArrivalEventHandler, LinkEnterEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {
    private final LinkFinder linkFinder;
    private final Collection<String> modes;
    private final PassengerTracker passengers = new PassengerTracker();

    private final List<PassengerRideItem> rides = new LinkedList<>();
    private final Map<Id<Person>, PassengerRideItem> currentRides = new HashMap<>();

    public PassengerAnalysisListener(Collection<String> modes, LinkFinder linkFinder) {
        this.linkFinder = linkFinder;
        this.modes = modes;
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            if (modes.contains(event.getLegMode())) {
                PassengerRideItem ride = new PassengerRideItem();
                rides.add(ride);

                ride.personId = event.getPersonId();
                ride.mode = event.getLegMode();

                ride.departureTime = event.getTime();
                ride.originLink = linkFinder.getLink(event.getLinkId());

                currentRides.put(event.getPersonId(), ride);
            }
        }
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
            double distance = linkFinder.getDistance(event.getLinkId());

            for (Id<Person> passengerId : passengers.getPassengerIds(event.getVehicleId())) {
                PassengerRideItem ride = currentRides.get(passengerId);

                if (ride == null) {
                    throw new IllegalStateException("Found vehicle enter link without departure");
                }

                ride.distance += distance;
            }
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
                PassengerRideItem ride = currentRides.get(event.getPersonId());

                if (ride == null) {
                    throw new IllegalStateException("Found vehicle enter event without departure");
                }

                ride.vehicleId = event.getVehicleId();
                ride.waitingTime = event.getTime() - ride.departureTime;

                passengers.addPassenger(event.getVehicleId(), event.getPersonId());
            }
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
                passengers.removePassenger(event.getVehicleId(), event.getPersonId());
            }
        }
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            PassengerRideItem ride = currentRides.remove(event.getPersonId());

            if (ride != null) {
                ride.arrivalTime = event.getTime();
                ride.destinationLink = linkFinder.getLink(event.getLinkId());
            }
        }
    }

    @Override
    public void reset(int iteration) {
        passengers.clear();
        rides.clear();
        currentRides.clear();
    }

    public List<PassengerRideItem> getRides() {
        return rides;
    }
}
