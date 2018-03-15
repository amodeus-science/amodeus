/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum LocationSpecs {
    DATABASE;

    private final Map<String, LocationSpec> map = new HashMap<>();

    public void put(LocationSpec locationSpec) {
        map.put(locationSpec.name(), locationSpec);
    }

    public LocationSpec fromString(String string) {
        return Objects.requireNonNull(map.get(string));
    }

}
