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

public class MultiPolygonsVirtualNetworkCreator<T, U> {

    private final VirtualNetwork<T> virtualNetwork;

    public MultiPolygonsVirtualNetworkCreator(MultiPolygons multipolygons, Collection<T> elements, //
            Function<T, Tensor> locationOf, Function<T, String> nameOf, Map<U, HashSet<T>> uElements, //
            Tensor lbounds, Tensor ubounds, boolean completeGraph) {
        this.virtualNetwork = createVirtualNetwork(multipolygons, //
                elements, locationOf, nameOf, uElements, lbounds, ubounds, completeGraph);
    }

    public VirtualNetwork<T> getVirtualNetwork() {
        return virtualNetwork;
    }

    private VirtualNetwork<T> createVirtualNetwork(MultiPolygons multipolygons, //
            Collection<T> elements, Function<T, Tensor> locationOf, Function<T, String> nameOf, //
            Map<U, HashSet<T>> uElements, Tensor lbounds, Tensor ubounds, boolean completeGraph) {

        Set<MultiPolygon> multipolygonsSet = multipolygons.getPolygons();
        System.out.println("creating a virtual network with " + multipolygonsSet.size() //
                + " multipolygons");

        // initialize new virtual network
        VirtualNetwork<T> virtualNetwork = new VirtualNetworkImpl<>();

        // for every polygon, create a virtualNode in its centroid and add all links in the polygon
        // to the virtualNode
        GeometryFactory factory = new GeometryFactory();
        Map<VirtualNode<T>, Set<T>> vNodeTMap = new LinkedHashMap<>();
        int vNodeIndex = 0;
        for (MultiPolygon polygon : multipolygons.getPolygons()) {
            String indexStr = VirtualNodes.getIdString(vNodeIndex);
            System.out.println(indexStr);
            Tensor centroid = Tensors.vector(polygon.getCentroid().getX(), //
                    polygon.getCentroid().getY());
            VirtualNode<T> virtualNode = //
                    new VirtualNode<>(vNodeIndex, indexStr, new HashMap<>(), centroid);
            vNodeTMap.put(virtualNode, new LinkedHashSet<T>());
            vNodeIndex++;

            // associate links to the node in which they are contained
            for (T t : elements) {
                Tensor tPos = locationOf.apply(t);
                Coordinate coordinate = new Coordinate(tPos.Get(0).number().doubleValue(), //
                        tPos.Get(1).number().doubleValue());
                if (polygon.contains(factory.createPoint(coordinate))) {
                    vNodeTMap.get(virtualNode).add(t);
                }
            }
            
            // ignore polygons that do not contain any link
            if(vNodeTMap.get(virtualNode).isEmpty()) {
                vNodeTMap.remove(virtualNode);
                vNodeIndex--;
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

}
