/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;

/** builds a trivial {@link VirtualNetwork} in which all elements T are
 * associated to one central {@link VirtualNode}
 *
 * @param <T> */
public class TrivialVirtualNetworkCreator<T> {

    private final VirtualNetwork<T> virtualNetwork;

    /** @param elements to form the {@link VirtualNetwork}
     * @param locationOf
     * @param nameOf */
    public TrivialVirtualNetworkCreator(//
            Collection<T> elements, Function<T, Tensor> locationOf, Function<T, String> nameOf) {
        virtualNetwork = createVirtualNetwork(elements, locationOf, nameOf);
    }

    private VirtualNetwork<T> createVirtualNetwork(//
            Collection<T> elements, Function<T, Tensor> locationOf, Function<T, String> nameOf) {

        System.out.println("creating a trivial virtual network with one virtual node spanning the \n" //
                + "entire network.");

        /** initialize new {@link VirtualNetwork} */
        VirtualNetwork<T> virtualNetwork = new VirtualNetworkImpl<>();

        int id = 0;
        String indexStr = VirtualNodes.getIdString(id);
        Tensor centroid = StaticHelper.meanOf(elements, locationOf);
        final VirtualNode<T> virtualNode = //
                new VirtualNode<>(id, indexStr, new HashMap<>(), centroid);

        final Set<T> set = new LinkedHashSet<>();
        for (T t : elements) {
            set.add(t);
        }

        Map<VirtualNode<T>, Set<T>> vNodeTMap = new LinkedHashMap<>();
        vNodeTMap.put(virtualNode, set);

        CreatorUtils.addToVNodes(vNodeTMap, nameOf, virtualNetwork);

        VirtualLinkBuilder.buildComplete(virtualNetwork);
        GlobalAssert.that(VirtualNetworkCheck.virtualLinkConsistencyCheck(virtualNetwork));

        // fill information for serialization
        CreatorUtils.fillSerializationInfo(elements, virtualNetwork, nameOf);

        return virtualNetwork;

    }

    public VirtualNetwork<T> getVirtualNetwork() {
        return virtualNetwork;
    }
}
