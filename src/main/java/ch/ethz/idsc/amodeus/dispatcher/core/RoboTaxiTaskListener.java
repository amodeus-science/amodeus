/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/* package */ interface RoboTaxiTaskListener {
    void handle(AVPickupTask avPickupTask);

    void handle(AVDropoffTask avDropoffTask);

    void handle(AVDriveTask avDriveTask);

    void handle(AVStayTask avStayTask);

}
