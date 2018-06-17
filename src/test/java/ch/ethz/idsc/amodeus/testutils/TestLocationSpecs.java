/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;

/* package */ enum TestLocationSpecs implements LocationSpec {
    SANFRANCISCO( //
            TestReferenceFrames.SANFRANCISCO, //
            new Coord(-122.4363005, 37.7511686)), // <- no cutting
    ;

    private final ReferenceFrame referenceFrame;
    // increasing the first value goes east
    // increasing the second value goes north
    private final Coord center;

    private TestLocationSpecs(ReferenceFrame referenceFrame, Coord center) {
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
