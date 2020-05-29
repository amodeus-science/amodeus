package org.matsim.amodeus.dvrp.schedule;

import org.matsim.contrib.dvrp.schedule.Task.TaskType;

public enum AmodeusTaskType implements TaskType {
    DRIVE, STAY, PICKUP, DROPOFF
}
