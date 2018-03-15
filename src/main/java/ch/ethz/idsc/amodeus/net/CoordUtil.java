/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class CoordUtil {
    public static Tensor toTensor(Coord coord) {
        return Tensors.vectorDouble(coord.getX(), coord.getY());
    }

    public static Coord toCoord(Tensor vector) {
        return new Coord(vector.Get(0).number().doubleValue(), vector.Get(1).number().doubleValue());
    }
}
