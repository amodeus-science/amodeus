/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MapSourceTest {

    @Test
    public void test() {
        for (MapSource mapSource : MapSource.values()) {
            assertEquals(mapSource.getTileSource().getName(), mapSource.name());
        }
    }

}
