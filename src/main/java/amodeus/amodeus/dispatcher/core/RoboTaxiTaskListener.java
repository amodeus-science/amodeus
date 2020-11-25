/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;

/* package */ interface RoboTaxiTaskListener {
    void handle(AmodeusStopTask avStopTask);

    void handle(AmodeusDriveTask avDriveTask);

    void handle(AmodeusStayTask avStayTask);

}
