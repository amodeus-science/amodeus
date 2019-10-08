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

    private final SortedMap<Integer, LinkSpeedTimeSeries> linkSet = new TreeMap<>();

    /** add a speed recording for @param link at @param time with a speed value [m/s] @param speed */
    public void addData(Integer linkIndex, int time, double speed) {
        if (linkSet.containsKey(linkIndex)) {
            LinkSpeedTimeSeries linkSpeeds = linkSet.get(linkIndex);
            linkSpeeds.setSpeed(time, speed);
        } else {
            linkSet.put(linkIndex, new LinkSpeedTimeSeries(time, speed));
        }
    }

    public SortedMap<Integer, LinkSpeedTimeSeries> getLinkSet() {
        return Collections.unmodifiableSortedMap(linkSet);
    }

    /** @return {@link Set} with all time steps for which a link
     *         speed was recorded on some {@link Link} */
    public Set<Integer> getRecordedTimes() {
        HashSet<Integer> recordedTimes = new HashSet<>();
        getLinkSet().values().stream().forEach(lsts -> {
            lsts.getRecordedTimes().stream().forEach(i -> {
                recordedTimes.add(i);
            });
        });
        return recordedTimes;
    }
}