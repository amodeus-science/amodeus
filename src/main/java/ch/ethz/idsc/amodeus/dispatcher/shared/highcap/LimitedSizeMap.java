/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.util.math.LruCache;

/** similar to {@link LruCache} */
// TODO use LruCache instead
public class LimitedSizeMap<K, V> extends LinkedHashMap<K, V> {
    /**
     * 
     */
    private static final long serialVersionUID = 5746554638924600201L;
    // ---
    private final int maxSize;

    public LimitedSizeMap(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

}
