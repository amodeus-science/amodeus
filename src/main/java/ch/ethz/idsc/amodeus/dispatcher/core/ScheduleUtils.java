/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;

import ch.ethz.matsim.av.schedule.AVStayTask;

/* package */ enum ScheduleUtils {
    ;
    public static boolean isLastTask(Schedule schedule, Task task) {
        return task.getTaskIdx() == schedule.getTaskCount() - 1;
    }

    public static boolean isNextToLastTask(Schedule schedule, Task task) {
        return task.getTaskIdx() == schedule.getTaskCount() - 2;
    }

    public static String scheduleOf(RoboTaxi robotaxi) {
        Schedule schedule = robotaxi.getSchedule();
        return toString(schedule);
    }

    /** @param robotaxi
     * @param taskEndTime has to be strictly less than scheduleEndTime
     * @param scheduleEndTime
     * @param destination */
    public static void makeWhole( //
            RoboTaxi robotaxi, double taskEndTime, double scheduleEndTime, Link destination) {
        if (taskEndTime < scheduleEndTime) {
            Schedule schedule = robotaxi.getSchedule();
            schedule.addTask(new AVStayTask(taskEndTime, scheduleEndTime, destination));
        } else {
            throw new IllegalArgumentException("taskEndTime " + taskEndTime + " > scheduleEndTime " + scheduleEndTime);
        }
    }

    /** function is useful for debugging
     * 
     * @param schedule
     * @return */
    public static String toString(Schedule schedule) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = false;
        int hiddenCount = 0;
        for (Task task : schedule.getTasks()) {
            boolean isStarted = task.getStatus().equals(Task.TaskStatus.STARTED);
            if (isStarted && !flag)
                stringBuilder.append("_X( " + hiddenCount + " ... )\n");
            flag |= isStarted;
            if (flag) {
                stringBuilder.append(isStarted ? ">" : " ");
                stringBuilder.append(task.toString());
                stringBuilder.append('\n');
            } else {
                ++hiddenCount;
            }
        }
        return stringBuilder.toString().trim();
    }

}
