package ch.ethz.matsim.av.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTask;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AVDriveTask extends DriveTask implements AVTaskWithRequests, AVTask {
	private final Set<AVRequest> requests = new HashSet<>();

	public AVDriveTask(VrpPathWithTravelData path) {
		super(AVTaskType.DRIVE, path);
	}

	public AVDriveTask(VrpPathWithTravelData path, Collection<AVRequest> requests) {
		this(path);
		this.requests.addAll(requests);
	}

	@Override
	public Set<AVRequest> getRequests() {
		return requests;
	}

	@Override
	public void addRequest(AVRequest request) {
		requests.add(request);
	}

	@Override
	public AVTaskType getAVTaskType() {
		return AVTaskType.DRIVE;
	}

	public boolean isUnoccupied() {
		return requests.isEmpty();
	}
}
