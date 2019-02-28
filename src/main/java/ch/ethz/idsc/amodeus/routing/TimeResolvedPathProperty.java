package ch.ethz.idsc.amodeus.routing;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** used to create a {@link NavigableMap} containing the entry times to {@link Link}s for a
 * {@link Path} */
public enum TimeResolvedPathProperty implements NetworkPropertyInterface<NavigableMap<Integer, Link>> {
    INSTANCE;

    @Override
    public NavigableMap<Integer, Link> fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now) {
        /** path */
        Path path = calculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), now, null, null);

        GlobalAssert.that(path.links.size() == path.nodes.size() - 1);

        NavigableMap<Integer, Link> linkEntryTimes = new TreeMap<>();
        Integer iNow = (int) (double) now;
        linkEntryTimes.put(iNow, from);

        Node nodePrev = from.getFromNode();
        Double timePrev = now;
        for (int i = 1; i < path.nodes.size(); ++i) {
            Path localPath = calculator.calcLeastCostPath(nodePrev, path.nodes.get(i), now, null, null);
            timePrev = now + localPath.travelTime;
            nodePrev = path.nodes.get(i);
            linkEntryTimes.put((int) (double) timePrev, path.links.get(i));
        }
        return linkEntryTimes;
    }
}
