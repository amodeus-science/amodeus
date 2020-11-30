/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskTypes;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.schedule.Task;

/** An {@link RoboTaxiTaskAdapter} is created using a {@link Task}, which is casted to
 * {@link AVTask} internally. The adapter then invokes the handling function
 * corresponding to one of the four possible {@link AVTaskType}s of the given
 * task. */
/* package */ class RoboTaxiTaskAdapter implements RoboTaxiTaskListener {

    public RoboTaxiTaskAdapter(Task task) {
        if (AmodeusTaskTypes.STOP.equals(task.getTaskType())) {
            handle((AmodeusStopTask) task);
        } else if (DrtDriveTask.TYPE.equals(task.getTaskType())) {
            handle((DrtDriveTask) task);
        } else if (DrtStayTask.TYPE.equals(task.getTaskType())) {
            handle((DrtStayTask) task);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void handle(AmodeusStopTask avStopTask) {
        // empty by design
    }

    @Override
    public void handle(DrtDriveTask avDriveTask) {
        // empty by design
    }

    @Override
    public void handle(DrtStayTask avStayTask) {
        // empty by design
    }

}
