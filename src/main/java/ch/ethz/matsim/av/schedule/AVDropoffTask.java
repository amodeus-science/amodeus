package ch.ethz.matsim.av.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AVDropoffTask extends StayTask implements AVTaskWithRequests, AVTask {
	private final Set<AVRequest> requests = new HashSet<>();

	public AVDropoffTask(double beginTime, double endTime, Link link) {
		super(AVTaskType.DROPOFF, beginTime, endTime, link);
	}

	public AVDropoffTask(double beginTime, double endTime, Link link, Collection<AVRequest> requests) {
		super(AVTaskType.DROPOFF, beginTime, endTime, link);

		this.requests.addAll(requests);
		for (AVRequest request : requests)
			request.setDropoffTask(this);
	}

	@Override
	public AVTaskType getAVTaskType() {
		return AVTaskType.DROPOFF;
	}

	@Override
	public Set<AVRequest> getRequests() {
		return requests;
	}

	@Override
	public void addRequest(AVRequest request) {
		requests.add(request);
		request.setDropoffTask(this);
	}
}
