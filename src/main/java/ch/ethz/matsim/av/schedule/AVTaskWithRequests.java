package ch.ethz.matsim.av.schedule;

import ch.ethz.matsim.av.passenger.AVRequest;

import java.util.Collection;

public interface AVTaskWithRequests {
	Collection<AVRequest> getRequests();
	void addRequest(AVRequest request);
}
