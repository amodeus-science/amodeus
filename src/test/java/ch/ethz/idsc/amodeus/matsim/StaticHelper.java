/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;
import ch.ethz.idsc.amodeus.testutils.TestLocationSpecs;

/* package */ enum StaticHelper {
    ;

    public static void setup() {
        for (LocationSpec locationSpec : TestLocationSpecs.values())
            LocationSpecDatabase.INSTANCE.put(locationSpec);
    }
}
