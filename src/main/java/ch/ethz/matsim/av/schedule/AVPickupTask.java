package ch.ethz.matsim.av.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AVPickupTask extends StayTask implements AVTaskWithRequests, AVTask {
	final private Set<AVRequest> requests = new HashSet<>();
	private final double earliestDepartureTime;

	public AVPickupTask(double beginTime, double endTime, Link link, double earliestDepartureTime) {
		super(AVTaskType.PICKUP, beginTime, endTime, link);
		this.earliestDepartureTime = earliestDepartureTime;
	}

	public AVPickupTask(double beginTime, double endTime, Link link, double earliestDepartureTime,
			Collection<AVRequest> requests) {
		this(beginTime, endTime, link, earliestDepartureTime);

		this.requests.addAll(requests);
		for (AVRequest request : requests)
			request.setPickupTask(this);
	}

	@Override
	public AVTaskType getAVTaskType() {
		return AVTaskType.PICKUP;
	}

	@Override
	public void addRequest(AVRequest request) {
		requests.add(request);
		request.setPickupTask(this);
	}

	@Override
	public Set<AVRequest> getRequests() {
		return requests;
	}

	public double getEarliestDepartureTime() {
		return earliestDepartureTime;
	}
}
