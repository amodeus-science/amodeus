/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class IdIntegerDatabase {
    private final Map<String, Integer> map = new HashMap<>();

    public int getId(String string) {
        return map.computeIfAbsent(string, s -> map.size());
    }

    public int size() {
        return map.size();
    }
}
