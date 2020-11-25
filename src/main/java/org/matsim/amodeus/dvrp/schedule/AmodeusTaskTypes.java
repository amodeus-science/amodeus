package org.matsim.amodeus.dvrp.schedule;

import org.matsim.contrib.drt.schedule.DrtTaskBaseType;
import org.matsim.contrib.drt.schedule.DrtTaskType;

public final class AmodeusTaskTypes {
    static public final DrtTaskType DRIVE = new DrtTaskType(DrtTaskBaseType.DRIVE);
    static public final DrtTaskType STOP = new DrtTaskType(DrtTaskBaseType.STOP);
    static public final DrtTaskType STAY = new DrtTaskType(DrtTaskBaseType.STAY);
}
