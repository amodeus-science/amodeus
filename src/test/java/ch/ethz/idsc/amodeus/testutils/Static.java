/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecs;

/* package */ enum Static {
    ;

    public static void setup() {
        for (LocationSpec locationSpec : UserLocationSpecs.values())
            LocationSpecs.DATABASE.put(locationSpec);
    }

}
