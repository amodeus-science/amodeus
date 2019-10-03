/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** DO NOT MODIFY CLASS
 * 
 * INSTANCES OF LinkSpeedTimeSeries
 * ARE USED IN MANY SCENARIOS */
public class LinkSpeedTimeSeries implements Serializable {

    /** keyMap contains times and Tensor a list of recorded speeds at the time */
    private /* non-final */ NavigableMap<Integer, Double> data;

    public LinkSpeedTimeSeries(int time, double speed) {
        GlobalAssert.that(time >= 0);
        data = new TreeMap<>();
        data.put(time, speed);
    }

    // TODO why needed?
    public Double getSpeedsAt(Integer time) {
        return data.get(time);
    }

    public Double getSpeedsFloor(Integer time) {
        return data.floorEntry(time).getValue();
    }

    public Integer getTimeFloor(Integer time) {
        return data.floorEntry(time).getKey();
    }

    public Set<Integer> getRecordedTimes() {
        return data.keySet();
    }

    /* package */ boolean containsTime(Integer time) {
        return data.containsKey(time);
    }

    public void setSpeed(Integer time, double speed) {
        GlobalAssert.that(speed >= 0);
        data.put(time, speed);
    }

}
