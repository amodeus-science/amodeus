/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** DO NOT MODIFY CLASS
 * 
 * INSTANCES OF LinkSpeedTimeSeries
 * ARE USED IN MANY SCENARIOS */
public class LinkSpeedTimeSeries implements Serializable {

    /** keyMap contains times and Tensor a list of recorded speeds at the time */
    private /* non-final */ NavigableMap<Integer, Tensor> data;

    public LinkSpeedTimeSeries(int time, double speed) {
        GlobalAssert.that(time >= 0);
        data = new TreeMap<>();
        data.put(time, Tensors.vector(speed));
    }

    public Tensor getSpeedsAt(Integer time) {
        return data.get(time);
    }
    
    public Tensor getSpeedsFloor(Integer time) {
        return data.floorEntry(time).getValue();
    }

    public Set<Integer> getRecordedTimes() {
        return data.keySet();
    }

    /* package */ boolean containsTime(Integer time) {
        return data.containsKey(time);
    }

    /* package */ void addSpeed(Integer time, double speed) {
        GlobalAssert.that(speed >= 0);

        if (data.containsKey(time)) {
            data.get(time).append(RealScalar.of(speed));
        } else {
            data.put(time, Tensors.vector(speed));
        }
    }

    public void resetSpeed(Integer time, double speed) {
        GlobalAssert.that(speed >= 0);
        data.put(time, Tensors.vector(speed));
    }

}
