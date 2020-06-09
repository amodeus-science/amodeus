/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Collectors;

import ch.ethz.idsc.tensor.io.Import;

public class StorageSupplier {

    @SuppressWarnings("unused")
    private final NavigableMap<Integer, File> navigableMap;
    private final List<File> ordered;
    private final int intervalEstimate;

    public StorageSupplier(NavigableMap<Integer, File> navigableMap) {
        this.navigableMap = navigableMap;
        ordered = new ArrayList<>(navigableMap.values());
        List<Integer> list = navigableMap.keySet().stream().limit(2).collect(Collectors.toList());
        // typically the list == [10, 20] and therefore the 20 - 10 == 10
        intervalEstimate = 2 <= list.size() ? list.get(1) - list.get(0) : 10;
    }

    /** @param index
     * @return
     * @throws Exception if anything goes wrong, for instance file not found,
     *             or object cannot be cast to SimulationObject */
    public SimulationObject getSimulationObject(int index) throws Exception {
        return Import.object(ordered.get(index));
    }

    public final int size() {
        return ordered.size();
    }

    public final int getIntervalEstimate() {
        return intervalEstimate;
    }
}
