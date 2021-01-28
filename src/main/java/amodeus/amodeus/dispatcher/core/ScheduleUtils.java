/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;

/* package */ enum ScheduleUtils {
    ;
    public static boolean isLastTask(Schedule schedule, Task task) {
        return task.getTaskIdx() == schedule.getTaskCount() - 1;
    }

    public static boolean isNextToLastTask(Schedule schedule, Task task) {
        return task.getTaskIdx() == schedule.getTaskCount() - 2;
    }

    public static String scheduleOf(RoboTaxi roboTaxi) {
        Schedule schedule = roboTaxi.getSchedule();
        return toString(schedule);
    }

    /** @param roboTaxi
     * @param taskEndTime has to be strictly less than scheduleEndTime
     * @param scheduleEndTime
     * @param destination */
    public static void makeWhole( //
            RoboTaxi roboTaxi, double taskEndTime, double scheduleEndTime, Link destination) {
        if (taskEndTime < scheduleEndTime) {
            Schedule schedule = roboTaxi.getSchedule();
            schedule.addTask(new DrtStayTask(taskEndTime, scheduleEndTime, destination));
        } else
            throw new IllegalArgumentException("taskEndTime " + taskEndTime + " > scheduleEndTime " + scheduleEndTime);
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
            } else
                ++hiddenCount;
        }
        return stringBuilder.toString().trim();
    }

}
