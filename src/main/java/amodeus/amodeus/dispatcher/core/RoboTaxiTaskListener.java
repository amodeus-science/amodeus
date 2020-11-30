/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;

/* package */ interface RoboTaxiTaskListener {
    void handle(AmodeusStopTask avStopTask);

    void handle(DrtDriveTask avDriveTask);

    void handle(DrtStayTask avStayTask);

}
