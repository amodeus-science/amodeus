/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LinkSpeedDataContainer implements Serializable {

    private static final long serialVersionUID = 5461313925304077143L;

    public SortedMap<Integer, LinkSpeedDataSet> linkSet = new TreeMap<>();

    public class LinkSpeedDataSet implements Serializable {

        private static final long serialVersionUID = -1040655013212850526L;

        public SortedMap<Integer, List<Double>> data = new TreeMap<>();
    }

    public void addData(int link, int time, double speed) {
        LinkSpeedDataSet container = new LinkSpeedDataSet();
        List<Double> list = new ArrayList<>();
        list.add(speed);
        if (linkSet.containsKey(link)) {
            container = linkSet.get(link);
            if (container.data.containsKey(time))
                container.data.get(time).add(speed);
            else
                container.data.put(time, list);
        } else {
            container.data.put(time, list);
            linkSet.put(link, container);
        }
    }
}