/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusPickupTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;

/* package */ interface RoboTaxiTaskListener {
    void handle(AmodeusPickupTask avPickupTask);

    void handle(AmodeusDropoffTask avDropoffTask);

    void handle(AmodeusDriveTask avDriveTask);

    void handle(AmodeusStayTask avStayTask);

}
