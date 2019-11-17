/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class VirtualNetworkImpl<T> implements VirtualNetwork<T>, Serializable {

    private final long virtualNetworkID; // to ensure that other objects dependent on virtualNetwork are derived of that particular network
    private final Map<Integer, VirtualNode<T>> virtualNodes = new LinkedHashMap<>();
    private final Map<Integer, VirtualLink<T>> virtualLinks = new LinkedHashMap<>();
    // the map is for checking that all links in the network are assigned to one vNode
    protected transient Map<T, VirtualNode<T>> networkElements = new LinkedHashMap<>();
    // is stored but only used to create references LINK
    private final Map<String, VirtualNode<T>> networkElementsSerializable = new LinkedHashMap<>();
    private final Map<Point, VirtualLink<T>> virtualLinkPairs = new LinkedHashMap<>();

    public VirtualNetworkImpl() {
        virtualNetworkID = System.currentTimeMillis();
    }

    @Override
    public Collection<VirtualNode<T>> getVirtualNodes() {
        return Collections.unmodifiableCollection(virtualNodes.values());
    }

    @Override
    public Collection<VirtualLink<T>> getVirtualLinks() {
        return Collections.unmodifiableCollection(virtualLinks.values());
    }

    @Override
    public int getvNodesCount() {
        return virtualNodes.size();
    }

    @Override
    public int getvLinksCount() {
        return virtualLinks.size();
    }

    @Override
    public final VirtualNode<T> getVirtualNode(T element) {
        GlobalAssert.that(Objects.nonNull(element));
        if (!networkElements.containsKey(element)) {
            throw new IllegalStateException("Element not found in VirtualNetwork: " + element.toString());
        }
        return networkElements.get(element);
    }

    @Override
    public final VirtualLink<T> getVirtualLink(int index) {
        return virtualLinks.get(index);
    }

    @Override
    public final VirtualNode<T> getVirtualNode(int index) {
        return virtualNodes.get(index);
    }

    /* package */ VirtualNode<T> addVirtualNode(VirtualNode<T> virtualNode) {
        if (virtualNodes.size() != virtualNode.getIndex()) {
            System.out.println(virtualNodes.size());
            System.out.println(virtualNode.getIndex());
        }
        GlobalAssert.that(virtualNodes.size() == virtualNode.getIndex()); // <- NEVER remove this check
        virtualNodes.put(virtualNode.getIndex(), virtualNode);
        virtualNode.getLinks().forEach(t -> networkElements.put(t, virtualNode));
        return virtualNode;
    }

    /* package */ void addVirtualLink(String idIn, VirtualNode<T> fromIn, VirtualNode<T> toIn, double distance) {
        GlobalAssert.that(Objects.nonNull(fromIn));
        GlobalAssert.that(Objects.nonNull(toIn));
        VirtualLink<T> virtualLink = new VirtualLink<>(virtualLinks.size(), idIn, fromIn, toIn, distance);
        virtualLinks.put(virtualLink.getIndex(), virtualLink);
        virtualLinkPairs.put(nodePair_key(fromIn, toIn), virtualLink);
    }

    private final Point nodePair_key(VirtualNode<T> fromIn, VirtualNode<T> toIn) {
        // it does not make sense to query links with source == dest:
        GlobalAssert.that(fromIn.getIndex() != toIn.getIndex());
        return new Point(fromIn.getIndex(), toIn.getIndex());
    }

    @Override
    public VirtualLink<T> getVirtualLink(VirtualNode<T> fromIn, VirtualNode<T> toIn) {
        return virtualLinkPairs.get(nodePair_key(fromIn, toIn));
    }

    @Override
    public boolean containsVirtualLink(VirtualNode<T> fromIn, VirtualNode<T> toIn) {
        return virtualLinkPairs.containsKey(nodePair_key(fromIn, toIn));
    }

    @Override
    public <U> Map<VirtualNode<T>, List<U>> createVNodeTypeMap() {
        return this.getVirtualNodes().stream().collect(Collectors.toMap(Function.identity(), vn -> new ArrayList<>()));
    }

    @Override
    public <U> Map<VirtualNode<T>, List<U>> binToVirtualNode(Collection<U> col, Function<U, T> function) {
        Map<VirtualNode<T>, List<U>> returnMap = createVNodeTypeMap();
        col.forEach(c -> returnMap.get(getVirtualNode(function.apply(c))).add(c));
        return returnMap;
    }

    private void fillLinkVNodeMap(Map<String, T> map) {
        networkElements = networkElementsSerializable.entrySet().stream().collect(Collectors.toMap( //
                e -> map.get(e.getKey()), //
                Map.Entry::getValue));
        checkConsistency();
    }

    @Override
    public void fillSerializationInfo(Map<String, T> map) {
        fillLinkVNodeMap(map);
        // Map<String, T> map = new HashMap<>();
        // network.getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));
        // virtualNodes.stream().forEach(v -> v.setLinksAfterSerialization2(map));
        virtualNodes.values().forEach(v -> v.setLinksAfterSerialization2(map));
    }

    protected void fillVNodeMapRAWVERYPRIVATE(Map<T, String> map) {
        GlobalAssert.that(!networkElements.isEmpty());
        networkElements.forEach((element, vNode) -> networkElementsSerializable.put(map.get(element), vNode));
        GlobalAssert.that(networkElements.size() == networkElementsSerializable.size());
        GlobalAssert.that(!networkElementsSerializable.isEmpty());
    }

    @Override
    public void checkConsistency() {
        GlobalAssert.that(!networkElementsSerializable.isEmpty());
        GlobalAssert.that(Objects.nonNull(networkElements));
    }

    @Override
    public long getvNetworkID() {
        return virtualNetworkID;
    }

    @Override
    public void printVirtualNetworkInfo() {
        System.out.println("virtual network " + getvNetworkID() + " has ");
        System.out.println(virtualNodes.size() + " virtual nodes and  " + virtualLinks.size() + " virtual links.");
        System.out.println("and is of abstract-Type " + VirtualNetworkImpl.class.getSimpleName());
    }
}
