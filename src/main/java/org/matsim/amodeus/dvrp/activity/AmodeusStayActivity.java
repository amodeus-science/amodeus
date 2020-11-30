package org.matsim.amodeus.dvrp.activity;

import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dynagent.FirstLastSimStepDynActivity;

public class AmodeusStayActivity extends FirstLastSimStepDynActivity {
    public static final String STAY_ACTIVITY_TYPE = "idle";
    final private DrtStayTask stayTask;

    public AmodeusStayActivity(DrtStayTask stayTask) {
        super(STAY_ACTIVITY_TYPE);
        this.stayTask = stayTask;
    }

    @Override
    protected boolean isLastStep(double now) {
        return stayTask.getEndTime() <= now;
    }
}
