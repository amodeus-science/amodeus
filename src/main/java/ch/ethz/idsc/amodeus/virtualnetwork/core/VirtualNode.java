/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;

//TODO @marcalbert document class
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
        linkIDsforSerialization.addAll(links.keySet());
        GlobalAssert.that(links.size() == linkIDsforSerialization.size());
    }

    /* package */ void setLinks(Map<String, T> links) {
        GlobalAssert.that(Objects.nonNull(this.links));
        GlobalAssert.that(this.links.size() == 0);
        links.forEach((s, t) -> {
            this.links.add(t);
            this.linkIDsforSerialization.add(s);
        });

        GlobalAssert.that(links.size() == linkIDsforSerialization.size());
    }

    /* package */ void setLinksAfterSerialization2(Map<String, T> map) {
        this.links = linkIDsforSerialization.stream().map(map::get).map(Objects::requireNonNull).collect(Collectors.toSet());
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
