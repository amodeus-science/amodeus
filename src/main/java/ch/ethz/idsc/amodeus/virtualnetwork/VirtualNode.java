/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;

public class VirtualNode<T> implements Serializable {
    /** index is counting from 0,1,... index is used to assign entries in vectors and matrices */
    private final int index;
    /** id is only used for debugging */
    private final String id;
    private transient Set<T> links;
    private final Set<String> linkIDsforSerialization = new HashSet<>();
    private final Tensor coord;

    public VirtualNode(int index, String id, Map<String, T> links, Tensor coord) {
        this.index = index;
        this.id = id;
        this.links = new HashSet<>(links.values());
        this.coord = coord;
        links.keySet().stream().forEach(linkIDsforSerialization::add);
        GlobalAssert.that(links.size() == linkIDsforSerialization.size());
    }

    /* package */ void setLinks(Map<String, T> links) {
        GlobalAssert.that(Objects.nonNull(this.links));
        GlobalAssert.that(this.links.size() == 0);
        for (Entry<String, T> entry : links.entrySet()) {
            this.links.add(entry.getValue());
            this.linkIDsforSerialization.add(entry.getKey());
        }
        GlobalAssert.that(links.size() == linkIDsforSerialization.size());
    }

    /* package */ void setLinksAfterSerialization2(Map<String, T> map) {
        this.links = new HashSet<>();
        for (String linkIDString : linkIDsforSerialization) {
            T link = map.get(linkIDString);
            GlobalAssert.that(Objects.nonNull(link));
            links.add(link);
        }
    }

    public Set<T> getLinks() {
        return Collections.unmodifiableSet(links);
    }

    public String getId() {
        return id;
    }

    public Tensor getCoord() {
        return coord;
    }

    public int getIndex() {
        return index;
    }

}
