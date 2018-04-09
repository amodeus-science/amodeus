/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class LinkSpeedDataContainer implements Serializable {
    private static final long serialVersionUID = 5461313925304077143L;

    public SortedMap<Integer, LinkSpeedTimeSeries> linkSet = new TreeMap<>();

    /** add a speed recording for @param link at @param time with a speed value [m/s] @param speed */
    public void addData(int link, int time, double speed) {

        if (linkSet.containsKey(link)) {
            LinkSpeedTimeSeries linkSpeeds = linkSet.get(link);
            linkSpeeds.addSpeed(time, speed);
        } else {
            linkSet.put(link, new LinkSpeedTimeSeries(time, speed));
        }
    }
}