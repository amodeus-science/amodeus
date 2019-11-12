/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;

public class LinkSpeedDataContainer implements Serializable {
    // linkMap saves for every link(Integer as IDs) a list with time(key)/speed(value)
    private final SortedMap<Integer, LinkSpeedTimeSeries> linkMap = new TreeMap<>();

    /** add a speed recording for @param link at @param time with a speed value [m/s] @param speed */
    public void addData(Integer linkIndex, int time, double speed) {
        // could be shortened if LinkSpeedTimeSeries was initialized empty
        if (linkMap.containsKey(linkIndex)) {
            // not sure why linkSpeeds was created before
            linkMap.get(linkIndex).setSpeed(time, speed);
        } // else creates new link with first speed/time record
        else {
            linkMap.put(linkIndex, new LinkSpeedTimeSeries(time, speed));
        }
    }

    public SortedMap<Integer, LinkSpeedTimeSeries> getLinkMap() {
        return Collections.unmodifiableSortedMap(linkMap);
    }

    /** @return {@link Set} with all time steps for which a link
     *         speed was recorded on some {@link Link} */
    public Set<Integer> getRecordedTimes() {
        HashSet<Integer> recordedTimes = new HashSet<>();
        getLinkMap().values().stream().map(LinkSpeedTimeSeries::getRecordedTimes).forEach(recordedTimes::addAll);
        return recordedTimes;
    }
}