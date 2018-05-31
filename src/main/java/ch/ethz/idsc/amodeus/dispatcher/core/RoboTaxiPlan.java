/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVStayTask;

public class RoboTaxiPlan {

    /* package */ static RoboTaxiPlan of(RoboTaxi robotaxi, double time) {
        return new RoboTaxiPlan(robotaxi, time);
    }

    public final NavigableMap<Double, RoboTaxiPlanEntry> plans = new TreeMap<>();

    private RoboTaxiPlan(RoboTaxi roboTaxi, double time) {

        Schedule schedule = roboTaxi.getSchedule();

        /** observed three cases: 1 task STAY, 2 tasks REB, 3 tasks with Customer
         * step 1 is to count the open tasks */
        NavigableMap<Double, Task> upComing = new TreeMap<>();
        for (Task task : schedule.getTasks()) {
            GlobalAssert.that(task.getBeginTime() <= task.getEndTime());
            if (task.getEndTime() >= time)
                upComing.put(task.getBeginTime(), task);
        }

        if (upComing.size() == 1) {
            Task stayTask = upComing.firstEntry().getValue();
            GlobalAssert.that(stayTask instanceof AVStayTask);
            RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry();
            entry.beginTime = stayTask.getBeginTime();
            entry.endTime = stayTask.getEndTime();
            entry.status = RoboTaxiStatus.STAY;
            plans.put(entry.beginTime, entry);
        }

        if (upComing.size() == 2) {
            int i = 0;
            for (Task task : upComing.values()) {
                ++i;
                RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry();
                entry.beginTime = task.getBeginTime();
                entry.endTime = task.getEndTime();
                if (i == 1) {
                    entry.status = RoboTaxiStatus.REBALANCEDRIVE;
                }
                if (i == 2) {
                    entry.status = RoboTaxiStatus.STAY;
                }
                plans.put(entry.beginTime, entry);
            }
        }

        if (upComing.size() == 3) {
            int i = 0;
            for (Task task : upComing.values()) {
                ++i;
                RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry();
                entry.beginTime = task.getBeginTime();
                entry.endTime = task.getEndTime();
                if (i == 1 || i == 2) {
                    entry.status = RoboTaxiStatus.DRIVEWITHCUSTOMER;
                }
                if (i == 2) {
                    entry.status = RoboTaxiStatus.STAY;
                }
                plans.put(entry.beginTime, entry);
            }
        }
    }
}
