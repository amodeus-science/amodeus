/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import ch.ethz.matsim.av.passenger.AVRequest;

/** for vehicles that are in stay task and should dropoff a customer at the link:
 * 1) finish stay task 2) append dropoff task 3) if more customers planned append drive task
 * 4) append new stay task */
/* package */ abstract class SharedFixedLocationDirective implements AbstractDirective {
    final RoboTaxi robotaxi;
    final AVRequest avRequest;
    final double getTimeNow;
    final double durationOfTask;

    public SharedFixedLocationDirective(RoboTaxi robotaxi, AVRequest avRequest, double getTimeNow, double durationOfTask) {
        this.robotaxi = robotaxi;
        this.avRequest = avRequest;
        this.getTimeNow = getTimeNow;
        this.durationOfTask = durationOfTask;
    }

}
