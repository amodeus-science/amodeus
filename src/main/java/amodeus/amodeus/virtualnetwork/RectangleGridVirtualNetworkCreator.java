/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.locationtech.jts.geom.Coordinate;

import amodeus.amodeus.util.math.Scalar2Number;
import amodeus.amodeus.virtualnetwork.core.AbstractVirtualNetworkCreator;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import amodeus.amodeus.virtualnetwork.core.VirtualNodes;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** Use to create a rectangular {@link VirtualNetwork}
 *
 * @param <T>
 * @param <U> */
public class RectangleGridVirtualNetworkCreator<T, U> extends AbstractVirtualNetworkCreator<T, U> {

    // private final VirtualNetwork<T> virtualNetwork;

    /** A rectangular virtual network with @param xDiv x @param yDiv cells will be created.
     * 
     * @param elements contained in the {@link VirtualNode}s
     * @param locationOf map to plane locations
     * @param nameOf map to string id
     * @param xBounds
     * @param yBounds
     * @param uElements
     * @param completeGraph */
    public RectangleGridVirtualNetworkCreator(Collection<T> elements, Function<T, Tensor> locationOf, //
            Function<T, String> nameOf, int xDiv, int yDiv, Tensor xBounds, Tensor yBounds, //
            Map<U, Set<T>> uElements, boolean completeGraph) {
        System.out.println("Creating a virtual rectangular virtual network with number of cells: ");
        System.out.println("( " + xDiv + " x " + yDiv + " )    lat x lng");

        /** compute data for creation of {@link VirtualNetwork} */
        Map<VirtualNode<T>, Set<T>> vNodeTMap = createAssignmentMap(elements, locationOf, xDiv, yDiv, //
                xBounds, yBounds);

        /** create */
        virtualNetwork = createVirtualNetwork(vNodeTMap, elements, uElements, nameOf, completeGraph);
    }

    private Map<VirtualNode<T>, Set<T>> createAssignmentMap(Collection<T> elements, Function<T, Tensor> locationOf, //
            int xDiv, int yDiv, Tensor xBounds, Tensor yBounds) {
        /** get network bounds */
        double xLength = Scalar2Number.of(xBounds.Get(1).subtract(xBounds.Get(0))).doubleValue() / xDiv;
        double yLength = Scalar2Number.of(yBounds.Get(1).subtract(yBounds.Get(0))).doubleValue() / yDiv;

        System.out.println(xBounds);
        System.out.println(yBounds);
        System.out.println("Cell dimensions: " + xLength + " x " + yLength);

        Map<VirtualNode<T>, Set<T>> vNodeTMap = new LinkedHashMap<>();
        int vNodeIndex = -1;
        for (int i = 0; i < xDiv; ++i)
            for (int j = 0; j < yDiv; ++j) {
                double xMin = Scalar2Number.of(xBounds.Get(0)).doubleValue() + i * xLength;
                double xMax = Scalar2Number.of(xBounds.Get(0)).doubleValue() + (i + 1) * xLength;
                double yMin = Scalar2Number.of(yBounds.Get(0)).doubleValue() + j * yLength;
                double yMax = Scalar2Number.of(yBounds.Get(0)).doubleValue() + (j + 1) * yLength;

                final Set<T> set = new LinkedHashSet<>();
                for (T t : elements) {
                    Tensor tPos = locationOf.apply(t);
                    Coordinate coordinate = new Coordinate(tPos.Get(0).number().doubleValue(), //
                            tPos.Get(1).number().doubleValue());
                    if (xMin <= coordinate.x && coordinate.x < xMax && //
                            yMin <= coordinate.y && coordinate.y < yMax)
                        set.add(t);
                }
                if (!set.isEmpty()) {
                    ++vNodeIndex;
                    String indexStr = VirtualNodes.getIdString(vNodeIndex);
                    System.out.println(indexStr);
                    Tensor centroid = Tensors.vector( //
                            (xMax - xMin) / 2 + xMin, //
                            (yMax - yMin) / 2 + yMin);
                    final VirtualNode<T> virtualNode = new VirtualNode<>(vNodeIndex, indexStr, new HashMap<>(), centroid);
                    vNodeTMap.put(virtualNode, set);
                }
            }
        return vNodeTMap;
    }

    @Override
    public VirtualNetwork<T> getVirtualNetwork() {
        return virtualNetwork;
    }
}
