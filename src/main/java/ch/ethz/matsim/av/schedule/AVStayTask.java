package ch.ethz.matsim.av.schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;

public class AVStayTask extends StayTask implements AVTask {
	public AVStayTask(double beginTime, double endTime, Link link) {
		super(AVTaskType.STAY, beginTime, endTime, link);
	}

	@Override
	public AVTaskType getAVTaskType() {
		return AVTaskType.STAY;
	}
}
