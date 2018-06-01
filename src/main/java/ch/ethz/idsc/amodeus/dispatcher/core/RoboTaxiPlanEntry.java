/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.io.Serializable;

public class RoboTaxiPlanEntry implements Serializable {
    public final double beginTime;
    public final double endTime;
    public final RoboTaxiStatus status;

    public RoboTaxiPlanEntry(double beginTime, double endTime, RoboTaxiStatus status) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.status = status;
    }
}
