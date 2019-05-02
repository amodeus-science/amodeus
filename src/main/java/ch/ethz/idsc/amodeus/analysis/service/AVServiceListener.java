/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.GenericEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.vehicles.Vehicle;

import ch.ethz.matsim.av.schedule.AVTransitEvent;

/* package */ class AVServiceListener implements PersonDepartureEventHandler, PersonArrivalEventHandler, GenericEventHandler, PersonEntersVehicleEventHandler,
        PersonLeavesVehicleEventHandler, LinkEnterEventHandler {
    final private Network network;
    final private AVServiceWriter writer;

    final private Map<Id<Person>, AVServiceItem> active = new HashMap<>();
    final private Map<Id<Person>, Integer> tripIndices = new HashMap<>();
    final private Map<Id<Vehicle>, Set<AVServiceItem>> passengers = new HashMap<>();

    public AVServiceListener(Network network, AVServiceWriter writer) {
        this.network = network;
        this.writer = writer;
    }

    @Override
    public void handleEvent(PersonDepartureEvent departureEvent) {
        int currentTripIndex = tripIndices.getOrDefault(departureEvent.getPersonId(), 0);

        if (departureEvent.getLegMode().equals("av")) {
            AVServiceItem item = new AVServiceItem();
            item.personId = departureEvent.getPersonId();
            item.tripIndex = currentTripIndex;
            item.departureTime = departureEvent.getTime();
            item.originLink = network.getLinks().get(departureEvent.getLinkId());
            item.distance = network.getLinks().get(departureEvent.getLinkId()).getLength();

            active.put(departureEvent.getPersonId(), item);
        }

        currentTripIndex += 1;
        tripIndices.put(departureEvent.getPersonId(), currentTripIndex);
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent enterEvent) {
        AVServiceItem item = active.get(enterEvent.getPersonId());

        if (item != null) {
            item.waitingTime = enterEvent.getTime() - item.departureTime;

            if (!passengers.containsKey(enterEvent.getVehicleId())) {
                passengers.put(enterEvent.getVehicleId(), new HashSet<>());
            }

            Set<AVServiceItem> vehiclePassengers = passengers.get(enterEvent.getVehicleId());
            vehiclePassengers.add(item);
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent leaveEvent) {
        AVServiceItem item = active.get(leaveEvent.getPersonId());

        if (item != null) {
            Set<AVServiceItem> vehiclePassengers = passengers.getOrDefault(leaveEvent.getVehicleId(), new HashSet<>());
            vehiclePassengers.remove(item);
        }
    }

    public void handleEvent(AVTransitEvent transitEvent) {
        AVServiceItem item = active.get(transitEvent.getPersonId());

        if (item != null) {
            item.chargedDistance = transitEvent.getDistance();
            item.operatorId = transitEvent.getOperatorId();
        }
    }

    @Override
    public void handleEvent(LinkEnterEvent linkEvent) {
        Set<AVServiceItem> items = passengers.get(linkEvent.getVehicleId());

        if (items != null) {
            double linkLength = network.getLinks().get(linkEvent.getLinkId()).getLength();
            items.forEach(item -> item.distance += linkLength);
        }
    }

    @Override
    public void handleEvent(PersonArrivalEvent arrivalEvent) {
        AVServiceItem item = active.remove(arrivalEvent.getPersonId());

        if (item != null) {
            item.destinationLink = network.getLinks().get(arrivalEvent.getLinkId());
            item.inVehicleTime = arrivalEvent.getTime() - item.departureTime - item.waitingTime;
            writer.write(item);
        }
    }

    @Override
    public void handleEvent(GenericEvent genericEvent) {
        if (genericEvent instanceof AVTransitEvent) {
            handleEvent((AVTransitEvent) genericEvent);
        }
    }
}
