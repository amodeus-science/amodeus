/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;

/* package */ enum ChicagoReferenceFrames implements ReferenceFrame {
    CHICAGO( //
            new GeotoolsTransformation("EPSG:3435", "WGS84"), //
            new GeotoolsTransformation("WGS84", "EPSG:3435")),//
    ;
    // ---
    private final CoordinateTransformation coords_toWGS84;
    private final CoordinateTransformation coords_fromWGS84;

    private ChicagoReferenceFrames(CoordinateTransformation c1, CoordinateTransformation c2) {
        coords_toWGS84 = c1;
        coords_fromWGS84 = c2;
    }

    @Override
    public CoordinateTransformation coords_fromWGS84() {
        return coords_fromWGS84;
    }

    @Override
    public CoordinateTransformation coords_toWGS84() {
        return coords_toWGS84;
    }
}
