/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum LocationSpecDatabase {
    INSTANCE;

    private final Map<String, LocationSpec> map = new HashMap<>();

    public void put(LocationSpec locationSpec) {
        map.put(locationSpec.name(), locationSpec);
    }

    /** @param string
     * @return
     * @throws Exception if string is not associated to a value */
    public LocationSpec fromString(String string) {
        LocationSpec locationSpec = map.get(string);
        if (Objects.isNull(locationSpec)) {
            System.err.println("LocationSpecDatabase miss: " + string);
            System.err.println("Please define LocationSpec in AmodeusOptions.properties, e.g.,");
            System.err.println("LocationSpec=SANFRANCISCO");
        }
        return Objects.requireNonNull(locationSpec);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
