/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/** Maintains a set of Links that minimize the Weber function value to
 * the past Links. The (discrete) Weber function is defined as the sum of of
 * Euclidean distances of the set. */
public class WeberMaintainer {
    private final Network network;
    private final List<Link> pastLinks;
    private Set<Link> minimizers;
    private Set<AVRequest> reqs = new HashSet<>();

    /** @param {@link Link} to initialize
     * @param network with {@link Link}s */
    public WeberMaintainer(Link link, Network network) {
        this.network = network;
        pastLinks = new ArrayList<>(Collections.singletonList(link));
        minimizers = new HashSet<>(pastLinks);
    }

    public void update(AVRequest avr) {
        if (reqs.add(avr))
            update(avr.getFromLink());
    }

    /** @return {@link Link} that currently minimizes the discrete
     *         Weber function to all past {@link Link}s */
    public Link getClosestMinimizer(Link to) {
        return minimizers.stream().min(Comparator.comparingDouble(l -> CoordUtils.calcEuclideanDistance(l.getCoord(), to.getCoord()))).get();
    }

    /** @return any {@link Link} in the collection of minimizers */
    public Link getAnyMinimizer() {
        return minimizers.iterator().next();
    }

    /** @param link {@link Link} is added to the past links */
    private void update(Link link) {
        pastLinks.add(link);
        findMin();
    }

    /** currently implemented as brute force that checks all the {@link Link}s.
     * Future extensions might include better heuristic, e.g. disk around past links
     * in the {@link Network} */
    private void findMin() {
        /** calculate values of discrete Weber function for all links */
        Map<Double, Set<Link>> distMap = network.getLinks().values().stream().collect(Collectors.groupingBy(this::weber, Collectors.toSet()));

        /** get Set of closest links */
        minimizers = distMap.entrySet().stream().min(Comparator.comparingDouble(Entry::getKey)).get().getValue();
    }

    private Double weber(Link link) {
        return pastLinks.stream().map(Link::getCoord).mapToDouble(coord -> CoordUtils.calcEuclideanDistance(link.getCoord(), coord)).sum();
    }

    public static void saveWeberLocations(Map<RoboTaxi, WeberMaintainer> maintainers, //
            File workingDirectory) throws IOException {
        File textFile = new File(workingDirectory, "weberFile");
        try (PrintWriter pw = new PrintWriter(textFile)) {
            for (WeberMaintainer wm : maintainers.values())
                pw.write(wm.getAnyMinimizer().getId().toString() + "\n");
        }
    }
}
