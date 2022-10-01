package org.matsim.amodeus.components.router;

import java.io.IOException;
import java.util.concurrent.Future;

import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.plpc.DefaultParallelLeastCostPathCalculator;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.modal.ModalProviders.InstanceGetter;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

/**
 * The DefaultAVRouter is a standard ParallelLeastCostPathCalculator using
 * Djikstra's algorithm.
 */
public class DefaultAmodeusRouter implements AmodeusRouter {
    public final static String TYPE = "Default";

    final private ParallelLeastCostPathCalculator delegate;

    DefaultAmodeusRouter(ParallelLeastCostPathCalculator delegate) {
        this.delegate = delegate;
    }

    @Override
    public Future<Path> calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person,
            Vehicle vehicle) {
        return delegate.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public static class Factory implements AmodeusRouter.Factory {
        @Override
        public AmodeusRouter createRouter(InstanceGetter inject) {
            Network network = (Network) inject.getModal(Network.class);
            TravelTime travelTime = (TravelTime) inject.getModal(TravelTime.class);
            AmodeusConfigGroup config = (AmodeusConfigGroup) inject.get(AmodeusConfigGroup.class);

            return new DefaultAmodeusRouter(DefaultParallelLeastCostPathCalculator.create(
                    (int) config.getNumberOfParallelRouters(), new DijkstraFactory(), network,
                    new OnlyTimeDependentTravelDisutility(travelTime), travelTime));
        }
    }
}
