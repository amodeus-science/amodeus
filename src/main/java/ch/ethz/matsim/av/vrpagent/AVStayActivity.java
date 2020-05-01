package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dynagent.DynActivity;

import ch.ethz.refactoring.schedule.AmodeusStayTask;

public class AVStayActivity implements DynActivity {
    final private AmodeusStayTask stayTask;
    private double now;
    private final String activityType;

    public AVStayActivity(AmodeusStayTask stayTask) {
        activityType = stayTask.getTaskType().toString();
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
