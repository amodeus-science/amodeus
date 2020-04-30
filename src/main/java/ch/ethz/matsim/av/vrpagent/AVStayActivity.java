package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dynagent.DynActivity;

import ch.ethz.matsim.av.schedule.AVStayTask;

public class AVStayActivity implements DynActivity {
	final private AVStayTask stayTask;
	private double now;
	private final String activityType;

	public AVStayActivity(AVStayTask stayTask) {
		activityType = stayTask.getAVTaskType().toString();
		this.stayTask = stayTask;
		this.now = stayTask.getBeginTime();
	}

	@Override
	public void doSimStep(double now) {
		this.now = now;
	}

	@Override
	public double getEndTime() {
		if (Double.isInfinite(stayTask.getEndTime())) {
			return now + 1;
		} else {
			return stayTask.getEndTime();
		}
	}

	@Override
	public String getActivityType() {
		return activityType;
	}

}
