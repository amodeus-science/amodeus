/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.data;

import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.CH1903LV03PlustoWGS84;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import org.matsim.core.utils.geometry.transformations.IdentityTransformation;
import org.matsim.core.utils.geometry.transformations.WGS84toCH1903LV03Plus;

public enum ReferenceFrames implements ReferenceFrame {
    IDENTITY( //
            new IdentityTransformation(), //
            new IdentityTransformation()), //
    SANFRANCISCO( //
            new GeotoolsTransformation("EPSG:26743", "WGS84"), //
            new GeotoolsTransformation("WGS84", "EPSG:26743")), //
    SIOUXFALLS( //
            new SiouxFallstoWGS84(), //
            new WGS84toSiouxFalls()), //
    SWITZERLAND( //
            new CH1903LV03PlustoWGS84(), //
            new WGS84toCH1903LV03Plus()), //
    ;
    // ---
    private final CoordinateTransformation coords_toWGS84;
    private final CoordinateTransformation coords_fromWGS84;

    private ReferenceFrames(CoordinateTransformation c1, CoordinateTransformation c2) {
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
