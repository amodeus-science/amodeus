/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.linkspeed;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class LinkSpeedDataContainer implements Serializable {
    // linkMap saves for every link(Integer as IDs) a list with time(key)/speed(value)
    private final SortedMap<Integer, LinkSpeedTimeSeries> linkMap = new TreeMap<>();

    public void addData(Link link, int time, double speed) {
        addData(link.getId(), time, speed);
    }

    public void addData(Id<Link> linkId, int time, double speed) {
        addData(linkId.index(), time, speed);
    }

    /** add a speed recording for link with
     * 
     * @param linkIndex at
     * @param time with a speed value
     * @param speed [m/s] */
    private void addData(Integer linkIndex, int time, double speed) {
        linkMap.computeIfAbsent(linkIndex, idx -> new LinkSpeedTimeSeries()). //
        /* linkMap.get(linkIndex) */ setSpeed(time, speed);
    }

    public SortedMap<Integer, LinkSpeedTimeSeries> getLinkMap() {
        return Collections.unmodifiableSortedMap(linkMap);
    }

    public LinkSpeedTimeSeries get(Link link) {
        return get(link.getId());
    }

    public LinkSpeedTimeSeries get(Id<Link> linkId) {
        return linkMap.get(linkId.index());
    }

    /** @return {@link Set} with all time steps for which a link speed was recorded on some {@link Link} */
    public Set<Integer> getRecordedTimes() {
        return linkMap.values().stream().map(LinkSpeedTimeSeries::getRecordedTimes) //
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }
}