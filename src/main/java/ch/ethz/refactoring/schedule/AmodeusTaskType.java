package ch.ethz.refactoring.schedule;

import org.matsim.contrib.dvrp.schedule.Task.TaskType;

public enum AmodeusTaskType implements TaskType {
    DRIVE, STAY, PICKUP, DROPOFF
}
