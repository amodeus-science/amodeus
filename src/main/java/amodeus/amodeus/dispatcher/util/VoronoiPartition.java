/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

public class VoronoiPartition<T> {
    private final Network network;
    private final QuadTree<T> tree;
    private Map<T, Set<Link>> partitions;
    private Function<T, Coord> location;

    public VoronoiPartition(Network network, Function<T, Coord> location) {
        this.network = network;
        this.location = location;
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        tree = new QuadTree<>(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
    }

    public void update(Collection<T> generators) {
        /** operation only well-defined if there are {@link RoboTaxi}s available */
        if (generators.size() > 0) {
            /** build {@link QuadTree} */
            tree.clear();
            generators.forEach(g -> {
                Coord coord = location.apply(g);
                tree.put(coord.getX(), coord.getY(), g);
            });

            /** create partitions */
            partitions = generators.stream().collect(Collectors.toMap(g -> g, g -> new HashSet<>()));

            network.getLinks().values().forEach(l -> {
                Set<Link> set = partitions.get(tree.getClosest(l.getCoord().getX(), l.getCoord().getY()));
                Objects.requireNonNull(set);
                set.add(l);
            });
        }
    }

    public Set<Link> of(T t) {
        return partitions.get(t);
    }
}
