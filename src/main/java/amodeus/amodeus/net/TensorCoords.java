/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.util.math.Scalar2Number;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.VectorQ;

public enum TensorCoords {
    ;
    /** returns the {@link Coord} as Tensor with Tensors.vectorDouble(coord.getX,coord.getY) */
    public static Tensor toTensor(Coord coord) {
        return Tensors.vectorDouble(coord.getX(), coord.getY());
    }

    /** returns the center of the VirtualNode as {@link Coord} */
    public static Coord vNodeToCoord(VirtualNode<Link> node) {
        return toCoord(node.getCoord());
    }

    /** @param vector of length 2
     * @return
     * @throws Exception if given vector does not have length 2 */
    public static Coord toCoord(Tensor vector) {
        VectorQ.requireLength(vector, 2); // ensure that vector of length 2;
        return new Coord( //
                Scalar2Number.of(vector.Get(0)).doubleValue(), //
                Scalar2Number.of(vector.Get(1)).doubleValue());
    }
}
