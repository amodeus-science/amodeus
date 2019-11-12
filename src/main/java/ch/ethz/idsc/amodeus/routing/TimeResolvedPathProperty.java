/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** used to create a {@link NavigableMap} containing the entry times to {@link Link}s for a
 * {@link Path} */
// TODO only used in amodidsc so maybe move in the future (make NetworkPropertyInterface public)
public enum TimeResolvedPathProperty implements NetworkPropertyInterface<NavigableMap<Double, Link>> {
    INSTANCE;

    @Override
    public NavigableMap<Double, Link> fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now) {
        /** path */
        Path path = PathProperty.INSTANCE.fromTo(from, to, calculator, now);
        GlobalAssert.that(path.links.size() == path.nodes.size() - 1);
        NavigableMap<Double, Link> linkEntryTimes = new TreeMap<>();

        // linkEntryTimes.put(now, from);
        // Node nodePrev = from.getFromNode();
        // double timePrev = now;
        // /** A path will have M nodes but only M-1 links, therefore the index i is used for nodes
        //  * and the index i-1 for the links */
        // for (int i = 1; i < path.nodes.size(); ++i) {
        //     Path localPath = calculator.calcLeastCostPath(nodePrev, path.nodes.get(i), now, null, null);
        //     timePrev = now + localPath.travelTime;
        //     nodePrev = path.nodes.get(i);
        //     linkEntryTimes.put(timePrev, path.links.get(i - 1));
        // }
        // GlobalAssert.that(linkEntryTimes.firstKey().equals(now));
        // return linkEntryTimes;

        double time = now;
        Iterator<Link> links = path.links.iterator();
        Iterator<Node> nodes = path.nodes.iterator();
        Node nodePrev = nodes.next();
        GlobalAssert.that(nodePrev.equals(from.getFromNode()));
        while (nodes.hasNext()) {
            Path localPath = PathProperty.INSTANCE.fromTo(nodePrev, nodePrev = nodes.next(), calculator, time);
            linkEntryTimes.put(time, links.next());
            time += localPath.travelTime;
        }
        GlobalAssert.that(linkEntryTimes.firstKey().equals(now));
        GlobalAssert.that(!links.hasNext());
        return linkEntryTimes;
    }
}
