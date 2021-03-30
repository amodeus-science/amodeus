/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.matsim.core.utils.collections.QuadTree;

import amodeus.amodeus.util.math.Scalar2Number;
import amodeus.amodeus.virtualnetwork.core.AbstractVirtualNetworkCreator;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import amodeus.amodeus.virtualnetwork.core.VirtualNodes;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class CentroidVirtualNetworkCreator<T, U> extends AbstractVirtualNetworkCreator<T, U> {

    public CentroidVirtualNetworkCreator(Collection<T> elements, List<T> centroids, Function<T, Tensor> locationOf, //
            Function<T, String> nameOf, Map<U, Set<T>> uElements, boolean completeGraph) {
        /** find min, max values for quad tree */
        NavigableSet<Scalar> xVals = new TreeSet<>();
        NavigableSet<Scalar> yVals = new TreeSet<>();
        elements.stream().map(locationOf).forEach(loc -> {
            xVals.add(loc.Get(0));
            yVals.add(loc.Get(1));
        });

        /** create a QuadTree with the centroid loations, requires minX, minY, maxX, maxY */
        QuadTree<T> quadTree = new QuadTree<>( //
                Scalar2Number.of(xVals.first()).doubleValue(), //
                Scalar2Number.of(yVals.first()).doubleValue(), //
                Scalar2Number.of(xVals.last()).doubleValue(), //
                Scalar2Number.of(yVals.last()).doubleValue());
        for (T t : centroids) {
            Tensor loc = locationOf.apply(t);
            quadTree.put( //
                    Scalar2Number.of(loc.Get(0)).doubleValue(), //
                    Scalar2Number.of(loc.Get(1)).doubleValue(), t);
        }

        /** create a virtual node for every centroid */
        Map<VirtualNode<T>, Set<T>> vNodeTMap = new LinkedHashMap<>();
        int id = -1;
        for (T centroid : centroids) {
            String indexStr = VirtualNodes.getIdString(id);
            vNodeTMap.put(new VirtualNode<>(++id, indexStr, new HashMap<>(), locationOf.apply(centroid)), new LinkedHashSet<>());
        }

        /** assign every element T to the closest centroid */
        VNodeAdd.byProximity(vNodeTMap, Tensors.of(xVals.first(), yVals.first()), Tensors.of(xVals.last(), yVals.last()), elements, locationOf);
        System.out.println("vNodeTmap size: " + vNodeTMap.size());

        /** create */
        virtualNetwork = createVirtualNetwork(vNodeTMap, elements, uElements, nameOf, completeGraph);
    }
}
