package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;

/* package */ enum MaxTwoMoreTasksAfterEndingOne {
    ;

    public static boolean check(Schedule schedule, Task task, double now, double timeStep) {
        if (LastTimeStep.check(task, now, timeStep)) {
            return task.getTaskIdx() >= schedule.getTaskCount() - 3;
        }
        return task.getTaskIdx() >= schedule.getTaskCount() - 2;
    }

}
