package ch.ethz.matsim.av.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;

import ch.ethz.matsim.av.generator.AmodeusIdentifiers;

public class FleetDistanceListener implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, LinkEnterEventHandler {
    private final LinkFinder linkFinder;
    private final PassengerTracker passengers = new PassengerTracker();
    private final Map<String, ModeData> data = new HashMap<>();

    static public class ModeData {
        public double occupiedDistance_m;
        public double emptyDistance_m;
        public double passengerDistance_m;
    }

    public FleetDistanceListener(Collection<String> modes, LinkFinder linkFinder) {
        this.linkFinder = linkFinder;

        for (String mode : modes) {
            data.put(mode, new ModeData());
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
                String mode = AmodeusIdentifiers.getMode(event.getVehicleId());

                if (data.containsKey(mode)) {
                    passengers.addPassenger(event.getVehicleId(), event.getPersonId());
                }
            }
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (!AmodeusIdentifiers.isValid(event.getPersonId())) {
            if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
                String mode = AmodeusIdentifiers.getMode(event.getVehicleId());

                if (data.containsKey(mode)) {
                    passengers.removePassenger(event.getVehicleId(), event.getPersonId());
                }
            }
        }
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (AmodeusIdentifiers.isValid(event.getVehicleId())) {
            String mode = AmodeusIdentifiers.getMode(event.getVehicleId());
            ModeData operator = data.get(mode);

            if (operator != null) {
                double linkLength = linkFinder.getDistance(event.getLinkId());
                int numberOfPassengers = passengers.getNumberOfPassengers(event.getVehicleId());

                if (numberOfPassengers > 0) {
                    operator.occupiedDistance_m += linkLength;
                } else {
                    operator.emptyDistance_m += linkLength;
                }

                operator.passengerDistance_m += linkLength * numberOfPassengers;
            }
        }
    }

    public Map<String, ModeData> getData() {
        return Collections.unmodifiableMap(data);
    }

    public ModeData getData(String mode) {
        return data.get(mode);
    }

    @Override
    public void reset(int iteration) {
        passengers.clear();

        for (String mode : new HashSet<>(data.keySet())) {
            data.put(mode, new ModeData());
        }
    }
}
