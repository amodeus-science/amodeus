/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;

/* package */ enum ChicagoLocationSpecs implements LocationSpec {
    CHICAGO( //
            ChicagoReferenceFrames.CHICAGO, //
            new Coord(-74.005, 40.712))//, // Updated), // <- no cutting
    ;

    private final ReferenceFrame referenceFrame;
    // increasing the first value goes right
    // increasing the second value goes north
    private final Coord center;

    private ChicagoLocationSpecs(ReferenceFrame referenceFrame, Coord center) {
        this.referenceFrame = referenceFrame;
        this.center = center;
    }

    @Override
    public ReferenceFrame referenceFrame() {
        return referenceFrame;
    }

    @Override
    public Coord center() {
        return center;
    }
}
