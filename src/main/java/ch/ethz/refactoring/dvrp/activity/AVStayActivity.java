package ch.ethz.refactoring.dvrp.activity;

import org.matsim.contrib.dynagent.FirstLastSimStepDynActivity;

import ch.ethz.refactoring.schedule.AmodeusStayTask;

public class AVStayActivity extends FirstLastSimStepDynActivity {
    final private AmodeusStayTask stayTask;

    public AVStayActivity(AmodeusStayTask stayTask) {
        super(stayTask.getTaskType().toString());
        this.stayTask = stayTask;
    }

    @Override
    protected boolean isLastStep(double now) {
        return stayTask.getEndTime() <= now;
    }
}
