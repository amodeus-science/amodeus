/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskTypes;
import org.matsim.contrib.dvrp.schedule.Task;

/** An {@link RoboTaxiTaskAdapter} is created using a {@link Task}, which is casted to
 * {@link AVTask} internally. The adapter then invokes the handling function
 * corresponding to one of the four possible {@link AVTaskType}s of the given
 * task. */
/* package */ class RoboTaxiTaskAdapter implements RoboTaxiTaskListener {

    public RoboTaxiTaskAdapter(Task task) {
        if (AmodeusTaskTypes.STOP.equals(task.getTaskType())) {
            handle((AmodeusStopTask) task);
        } else if (AmodeusTaskTypes.DRIVE.equals(task.getTaskType())) {
            handle((AmodeusDriveTask) task);
        } else if (AmodeusTaskTypes.STAY.equals(task.getTaskType())) {
            handle((AmodeusStayTask) task);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void handle(AmodeusStopTask avStopTask) {
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
