package org.matsim.amodeus.dvrp.schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;

public class AmodeusStayTask extends StayTask {
    public AmodeusStayTask(double beginTime, double endTime, Link link) {
        super(AmodeusTaskType.STAY, beginTime, endTime, link);
    }
}
