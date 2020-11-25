package org.matsim.amodeus.dvrp.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTask;

public class AmodeusDriveTask extends DriveTask {
    private final Set<PassengerRequest> requests = new HashSet<>();

    public AmodeusDriveTask(VrpPathWithTravelData path) {
        super(AmodeusTaskTypes.DRIVE, path);
    }

    public AmodeusDriveTask(VrpPathWithTravelData path, Collection<PassengerRequest> requests) {
        this(path);
        this.requests.addAll(requests);
    }
}
