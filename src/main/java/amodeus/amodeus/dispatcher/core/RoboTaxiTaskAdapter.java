/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusPickupTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskType;
import org.matsim.contrib.dvrp.schedule.Task;

/** An {@link RoboTaxiTaskAdapter} is created using a {@link Task}, which is casted to
 * {@link AVTask} internally. The adapter then invokes the handling function
 * corresponding to one of the four possible {@link AVTaskType}s of the given
 * task. */
/* package */ class RoboTaxiTaskAdapter implements RoboTaxiTaskListener {

    public RoboTaxiTaskAdapter(Task task) {
        switch ((AmodeusTaskType) task.getTaskType()) {
        case PICKUP:
            handle((AmodeusPickupTask) task);
            break;
        case DROPOFF:
            handle((AmodeusDropoffTask) task);
            break;
        case DRIVE:
            handle((AmodeusDriveTask) task);
            break;
        case STAY:
            handle((AmodeusStayTask) task);
        }
    }

    @Override
    public void handle(AmodeusPickupTask avPickupTask) {
        // empty by design
    }

    @Override
    public void handle(AmodeusDropoffTask avDropoffTask) {
        // empty by design
    }

    @Override
    public void handle(AmodeusDriveTask avDriveTask) {
        // empty by design
    }

    @Override
    public void handle(AmodeusStayTask avStayTask) {
        // empty by design
    }

}
