package ch.ethz.matsim.av.waiting_time;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public interface WaitingTimeCollector {
	void registerWaitingTime(double time, double waitingTime, Id<Link> linkId);

	void consolidate();
}
