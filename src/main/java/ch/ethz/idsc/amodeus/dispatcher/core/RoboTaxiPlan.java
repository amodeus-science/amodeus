/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVStayTask;

/* package */ class RoboTaxiPlan implements Serializable {

    /* package */ static RoboTaxiPlan of(Schedule schedule, double time) {
        return new RoboTaxiPlan(schedule, time);
    }

    private final NavigableMap<Double, RoboTaxiPlanEntry> plans = new TreeMap<>();

    private RoboTaxiPlan(Schedule schedule, double time) {

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
            RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry( //
                    stayTask.getBeginTime(), //
                    stayTask.getEndTime(), //
                    RoboTaxiStatus.STAY);
            plans.put(entry.beginTime, entry);
        } else //

        if (upComing.size() == 2) {
            int i = 0;
            for (Task task : upComing.values()) {
                ++i;
                RoboTaxiStatus status = null;
                if (i == 1) {
                    status = RoboTaxiStatus.REBALANCEDRIVE;
                } else //
                if (i == 2) {
                    status = RoboTaxiStatus.STAY;
                }
                RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry( //
                        task.getBeginTime(), //
                        task.getEndTime(), //
                        status);
                plans.put(entry.beginTime, entry);
            }
        } else //

        if (upComing.size() == 3) {
            int i = 0;
            for (Task task : upComing.values()) {
                ++i;
                RoboTaxiStatus status = null;
                if (i == 1 || i == 2) {
                    status = RoboTaxiStatus.DRIVEWITHCUSTOMER;
                } else //
                if (i == 3) {
                    status = RoboTaxiStatus.STAY;
                }
                RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry( //
                        task.getBeginTime(), //
                        task.getEndTime(), //
                        status);
                plans.put(entry.beginTime, entry);
            }
        }
    }

    public NavigableMap<Double, RoboTaxiPlanEntry> getPlans() {
        return Collections.unmodifiableNavigableMap(plans);
    }
}
