package ch.ethz.matsim.av.schedule;

import org.matsim.contrib.dvrp.schedule.Task;

public interface AVTask extends Task {
	enum AVTaskType implements TaskType {
		PICKUP, DROPOFF, DRIVE, STAY
	}
	
	AVTaskType getAVTaskType();

}
