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

import ch.ethz.idsc.amodeus.virtualnetwork.core.AbstractVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNodes;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Mean;

/** builds a trivial {@link VirtualNetwork} in which all elements T are
 * associated to one central {@link VirtualNode}
 *
 * @param <T> */
public class TrivialVirtualNetworkCreator<T, U> extends AbstractVirtualNetworkCreator<T, U> {

    /** @param elements to form the {@link VirtualNetwork}
     * @param locationOf
     * @param nameOf */
    public TrivialVirtualNetworkCreator(//
            Collection<T> elements, Function<T, Tensor> locationOf, Function<T, String> nameOf, //
            Map<U, HashSet<T>> uElements, boolean completeGraph) {

        /** compute data for creation of {@link VirtualNetwork} */
        Map<VirtualNode<T>, Set<T>> vNodeTMap = createAssignmentMap(elements, locationOf, nameOf);

        /** create */
        virtualNetwork = createVirtualNetwork(vNodeTMap, elements, uElements, nameOf, completeGraph);
    }

    private Map<VirtualNode<T>, Set<T>> createAssignmentMap(//
            Collection<T> elements, Function<T, Tensor> locationOf, Function<T, String> nameOf) {

        System.out.println("creating a trivial virtual network with one virtual node spanning the \n" //
                + "entire network.");

        int id = 0;
        String indexStr = VirtualNodes.getIdString(id);
        Tensor centroid = meanOf(elements, locationOf);
        final VirtualNode<T> virtualNode = //
                new VirtualNode<>(id, indexStr, new HashMap<>(), centroid);

        final Set<T> set = new LinkedHashSet<>();
        for (T t : elements) {
            set.add(t);
        }

        Map<VirtualNode<T>, Set<T>> vNodeTMap = new LinkedHashMap<>();
        vNodeTMap.put(virtualNode, set);

        return vNodeTMap;

    }

    public static <T> Tensor meanOf(Collection<T> elements, Function<T, Tensor> locationOf) {
        return Mean.of(Tensor.of(elements.stream().map(locationOf::apply)));
    }

}
