package org.matsim.amodeus.dvrp.activity;

import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.contrib.dynagent.FirstLastSimStepDynActivity;

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
