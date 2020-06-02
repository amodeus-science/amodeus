/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;

import amodeus.amodeus.util.math.GlobalAssert;

// TODO @clruch should this be consistent with schedule?
/* package */ class RoboTaxiPlan implements Serializable {

    /* package */ static RoboTaxiPlan of(Schedule schedule, double time) {
        return new RoboTaxiPlan(schedule, time);
    }

    // ---
    private final NavigableMap<Double, RoboTaxiPlanEntry> plans = new TreeMap<>();

    private RoboTaxiPlan(Schedule schedule, double time) {
        /** observed three cases: 1 task STAY, 2 tasks REB, 3 tasks with Customer
         * step 1 is to count the open tasks */
        // NavigableMap<Double, Task> upComing = new TreeMap<>();
        // for (Task task : schedule.getTasks()) {
        // GlobalAssert.that(task.getBeginTime() <= task.getEndTime());
        // if (task.getEndTime() >= time)
        // upComing.put(task.getBeginTime(), task);
        // }
        //
        // if (upComing.size() == 1) {
        // Task stayTask = upComing.firstEntry().getValue();
        // GlobalAssert.that(stayTask instanceof AVStayTask);
        // RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry( //
        // stayTask.getBeginTime(), //
        // stayTask.getEndTime(), //
        // RoboTaxiStatus.STAY);
        // plans.put(entry.beginTime, entry);
        // } else //
        //
        // if (upComing.size() == 2) {
        // int i = 0;
        // for (Task task : upComing.values()) {
        // ++i;
        // RoboTaxiStatus status = null;
        // if (i == 1) {
        // status = RoboTaxiStatus.REBALANCEDRIVE;
        // } else //
        // if (i == 2) {
        // status = RoboTaxiStatus.STAY;
        // }
        // RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry( //
        // task.getBeginTime(), //
        // task.getEndTime(), //
        // status);
        // plans.put(entry.beginTime, entry);
        // }
        // } else //
        //
        // if (upComing.size() == 3) {
        // int i = 0;
        // for (Task task : upComing.values()) {
        // ++i;
        // RoboTaxiStatus status = null;
        // if (i == 1 || i == 2) {
        // status = RoboTaxiStatus.DRIVEWITHCUSTOMER;
        // } else //
        // if (i == 3) {
        // status = RoboTaxiStatus.STAY;
        // }
        // RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry( //
        // task.getBeginTime(), //
        // task.getEndTime(), //
        // status);
        // plans.put(entry.beginTime, entry);
        // }
        // }

        GlobalAssert.that(schedule.getTasks().stream().allMatch(task -> task.getBeginTime() <= task.getEndTime()));
        List<Task> upComing = schedule.getTasks().stream().filter(task -> task.getEndTime() >= time).collect(Collectors.toList());
        LinkedList<RoboTaxiStatus> statuses = new LinkedList<>();
        switch (upComing.size()) {
        case 1:
            statuses.add(RoboTaxiStatus.STAY);
            break;
        case 2:
            statuses.add(RoboTaxiStatus.REBALANCEDRIVE);
            statuses.add(RoboTaxiStatus.STAY);
            break;
        case 3:
            statuses.add(RoboTaxiStatus.DRIVEWITHCUSTOMER);
            statuses.add(RoboTaxiStatus.DRIVEWITHCUSTOMER);
            statuses.add(RoboTaxiStatus.STAY);
        }

        for (Task task : upComing) {
            RoboTaxiPlanEntry entry = new RoboTaxiPlanEntry( //
                    task.getBeginTime(), //
                    task.getEndTime(), //
                    statuses.poll());
            plans.put(entry.beginTime, entry);
        }
    }

    public NavigableMap<Double, RoboTaxiPlanEntry> getPlans() {
        return Collections.unmodifiableNavigableMap(plans);
    }
}
