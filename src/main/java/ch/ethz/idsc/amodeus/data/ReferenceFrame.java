/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.data;

import org.matsim.core.utils.geometry.CoordinateTransformation;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.qty.Unit;

/** the ScenarioViewer requires coordinates in WGS84
 * that uses latitude and longitude.
 * However, the matsim scenario encodes link coordinates often
 * in local coordinates in [m].
 * ReferenceFrame maps between the two. */
public interface ReferenceFrame {
    /** @return transformation from WGS84 to simulation coordinates */
    CoordinateTransformation coords_fromWGS84();

    /** @return transformation from simulation coordinates to WGS84 */
    CoordinateTransformation coords_toWGS84();

    /** @return distance unit */
    default Unit unit() {
        return SI.METER;
    }
}
