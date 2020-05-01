/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import ch.ethz.refactoring.schedule.AmodeusDriveTask;
import ch.ethz.refactoring.schedule.AmodeusDropoffTask;
import ch.ethz.refactoring.schedule.AmodeusPickupTask;
import ch.ethz.refactoring.schedule.AmodeusStayTask;

/* package */ interface RoboTaxiTaskListener {
    void handle(AmodeusPickupTask avPickupTask);

    void handle(AmodeusDropoffTask avDropoffTask);

    void handle(AmodeusDriveTask avDriveTask);

    void handle(AmodeusStayTask avStayTask);

}
