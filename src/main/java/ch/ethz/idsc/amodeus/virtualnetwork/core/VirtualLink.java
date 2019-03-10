/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.io.Serializable;

public class VirtualLink<T> implements Serializable {
    /** index is counting from 0,1,...
     * index is used to assign entries in vectors and matrices */
    private final int index;
    private final String id;
    private final VirtualNode<T> from;
    private final VirtualNode<T> to;
    private final double distance;

    VirtualLink(int index, String id, VirtualNode<T> from, VirtualNode<T> to, double distance) {
        this.index = index;
        this.id = id;
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public VirtualNode<T> getFrom() {
        return from;
    }

    public VirtualNode<T> getTo() {
        return to;
    }

    public double getDistance() {
        return distance;
    }

    public int getIndex() {
        return index;
    }
}
