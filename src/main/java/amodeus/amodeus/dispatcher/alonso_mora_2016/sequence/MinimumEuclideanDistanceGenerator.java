package amodeus.amodeus.dispatcher.alonso_mora_2016.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.utils.geometry.CoordUtils;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class MinimumEuclideanDistanceGenerator implements Iterator<List<StopDirective>> {
    private final List<StopDirective> solution;
    private boolean hasNext = true;

    public MinimumEuclideanDistanceGenerator(Link startLink, List<PassengerRequest> existingRequests, List<PassengerRequest> addedRequests) {
        List<StopDirective> directives = new ArrayList<>(existingRequests.size() + addedRequests.size() * 2);
        existingRequests.forEach(r -> directives.add(Directive.dropoff(r)));
        addedRequests.forEach(r -> directives.add(Directive.pickup(r)));
        addedRequests.forEach(r -> directives.add(Directive.dropoff(r)));

        Link currentLink = startLink;

        IdSet<Request> pickedUpIds = new IdSet<>(Request.class);
        existingRequests.forEach(r -> pickedUpIds.add(r.getId()));

        solution = new ArrayList<>(directives.size());

        while (directives.size() > 0) {
            double minimumDistance = Double.POSITIVE_INFINITY;
            StopDirective minimumDistanceDirective = null;

            for (StopDirective directive : directives) {
                boolean isFeasible = true;

                if (!directive.isPickup()) {
                    isFeasible = pickedUpIds.contains(directive.getRequest().getId());
                }

                if (isFeasible) {
                    double distance = CoordUtils.calcEuclideanDistance(currentLink.getCoord(), Directive.getLink(directive).getCoord());

                    if (distance < minimumDistance) {
                        minimumDistance = distance;
                        minimumDistanceDirective = directive;
                    }
                }
            }

            directives.remove(minimumDistanceDirective);
            currentLink = Directive.getLink(minimumDistanceDirective);
            solution.add(minimumDistanceDirective);

            if (minimumDistanceDirective.isPickup()) {
                pickedUpIds.add(minimumDistanceDirective.getRequest().getId());
            }
        }
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public List<StopDirective> next() {
        hasNext = false;
        return solution;
    }

    static public class Factory implements SequenceGeneratorFactory {
        @Override
        public Iterator<List<StopDirective>> create(Link startLink, List<PassengerRequest> existingRequests, List<PassengerRequest> addedRequests) {
            return new MinimumEuclideanDistanceGenerator(startLink, existingRequests, addedRequests);
        }
    }
}
