package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.contrib.dvrp.schedule.Task;

/* package */ enum LastTimeStep {
    ;

    public static boolean check(Task task, double now, double timeStep) {
        return task.getEndTime() < now + timeStep;
    }

}
