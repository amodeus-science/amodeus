package ch.ethz.refactoring.dvrp.activity;

import org.matsim.contrib.dynagent.FirstLastSimStepDynActivity;

import ch.ethz.refactoring.schedule.AmodeusStayTask;

public class AmodeusStayActivity extends FirstLastSimStepDynActivity {
    public static final String STAY_ACTIVITY_TYPE = "idle";
    final private AmodeusStayTask stayTask;

    public AmodeusStayActivity(AmodeusStayTask stayTask) {
        super(STAY_ACTIVITY_TYPE);
        this.stayTask = stayTask;
    }

    @Override
    protected boolean isLastStep(double now) {
        return stayTask.getEndTime() <= now;
    }
}
