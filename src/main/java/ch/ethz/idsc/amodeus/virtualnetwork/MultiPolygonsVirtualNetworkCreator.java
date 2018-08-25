/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** @param <T> the class on which the {@link VirtualNetwork} is defined, e.g., Link
 * @param <U> */
public class MultiPolygonsVirtualNetworkCreator<T, U> {

    private final VirtualNetwork<T> virtualNetwork;

    public MultiPolygonsVirtualNetworkCreator(MultiPolygons multipolygons, Collection<T> elements, //
            Function<T, Tensor> locationOf, Function<T, String> nameOf, Map<U, HashSet<T>> uElements, //
            Tensor lbounds, Tensor ubounds, boolean completeGraph) {
        this.virtualNetwork = createVirtualNetwork( //
                multipolygons, elements, locationOf, nameOf, uElements, lbounds, ubounds, completeGraph);
    }

    private VirtualNetwork<T> createVirtualNetwork(MultiPolygons multipolygons, //
            Collection<T> elements, Function<T, Tensor> locationOf, Function<T, String> nameOf, //
            Map<U, HashSet<T>> uElements, Tensor lbounds, Tensor ubounds, boolean completeGraph) {

        System.out.println("creating a virtual network with " + multipolygons.getPolygons().size() //
                + " multipolygons");

        /** initialize new {@link VirtualNetwork} */
        VirtualNetwork<T> virtualNetwork = new VirtualNetworkImpl<>();

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

        CreatorUtils.addToVNodes(vNodeTMap, nameOf, virtualNetwork);

        // create virtualLinks for complete or neighboring graph
        VirtualLinkBuilder.build(virtualNetwork, completeGraph, uElements);
        GlobalAssert.that(VirtualNetworkCheck.virtualLinkConsistencyCheck(virtualNetwork));

        // fill information for serialization
        CreatorUtils.fillSerializationInfo(elements, virtualNetwork, nameOf);

        return virtualNetwork;
    }

    public VirtualNetwork<T> getVirtualNetwork() {
        return virtualNetwork;
    }
}
