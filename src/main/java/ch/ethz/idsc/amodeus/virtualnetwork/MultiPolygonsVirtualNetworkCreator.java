/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;

import ch.ethz.idsc.amodeus.virtualnetwork.core.AbstractVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNodes;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** @param <T> the class on which the {@link VirtualNetwork} is defined, e.g., Link
 * @param <U> */
public class MultiPolygonsVirtualNetworkCreator<T, U> extends AbstractVirtualNetworkCreator<T, U> {

    // private final VirtualNetwork<T> virtualNetwork;

    public MultiPolygonsVirtualNetworkCreator(MultiPolygons multipolygons, Collection<T> elements, //
            Function<T, Tensor> locationOf, Function<T, String> nameOf, Map<U, Set<T>> uElements, //
            Tensor lbounds, Tensor ubounds, boolean completeGraph) {

        Map<VirtualNode<T>, Set<T>> vNodeTMap = createAssignmentMap( //
                multipolygons, elements, locationOf, nameOf, uElements, completeGraph);

        /** create */
        virtualNetwork = createVirtualNetwork(vNodeTMap, elements, uElements, nameOf, completeGraph);

    }

    private Map<VirtualNode<T>, Set<T>> createAssignmentMap(MultiPolygons multipolygons, //
            Collection<T> elements, Function<T, Tensor> locationOf, Function<T, String> nameOf, //
            Map<U, Set<T>> uElements, boolean completeGraph) {

        System.out.println("creating a virtual network with " + multipolygons.getPolygons().size() //
                + " multipolygons");

        /** for every polygon, create virtualNode in centroid,add all links in the polygon
         * to the virtualNode */
        GeometryFactory factory = new GeometryFactory();
        Map<VirtualNode<T>, Set<T>> vNodeTMap = new LinkedHashMap<>();
        int vNodeIndex = 0;
        for (MultiPolygon polygon : multipolygons.getPolygons()) {
            System.out.println("MultiPolygonsVirtualNetworkCreator creates a Virtual Network \n" + //
                    "based on your .shp file. This operation may take long for large \n" + //
                    "number of points in your geometry.");
            System.out.println("Current polygon has " + polygon.getNumPoints() + " points.");
            final Set<T> set = new LinkedHashSet<>();
            /** associate links to the node in which they are contained */
            for (T t : elements) {
                Tensor tPos = locationOf.apply(t);
                Coordinate coordinate = new Coordinate(tPos.Get(0).number().doubleValue(), //
                        tPos.Get(1).number().doubleValue());
                if (polygon.contains(factory.createPoint(coordinate)))
                    set.add(t);
            }

            if (!set.isEmpty()) {

                String indexStr = VirtualNodes.getIdString(vNodeIndex);
                System.out.println(indexStr);
                Tensor centroid = Tensors.vector( //
                        polygon.getCentroid().getX(), //
                        polygon.getCentroid().getY());
                final VirtualNode<T> virtualNode = //
                        new VirtualNode<>(vNodeIndex, indexStr, new HashMap<>(), centroid);
                vNodeTMap.put(virtualNode, set);
                ++vNodeIndex;
            }
        }

        return vNodeTMap;

    }
}
