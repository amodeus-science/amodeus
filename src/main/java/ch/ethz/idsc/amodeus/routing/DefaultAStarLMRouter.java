/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import java.io.IOException;
import java.util.concurrent.Future;

import org.matsim.amodeus.components.AVRouter;
import org.matsim.amodeus.plpc.DefaultParallelLeastCostPathCalculator;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

/** The DefaultAStarLMRouter is a standard ParallelLeastCostPathCalculator using
 * A* Landmarks routing algorithm. */
public class DefaultAStarLMRouter implements AVRouter {
    final private ParallelLeastCostPathCalculator delegate;

    DefaultAStarLMRouter(ParallelLeastCostPathCalculator delegate) {
        this.delegate = delegate;
    }

    @Override
    public Future<Path> calcLeastCostPath(Node fromNode, Node toNode, double starttime, //
            Person person, Vehicle vehicle) {
        return delegate.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public static class Factory implements AVRouter.Factory {
        @Override
        public AVRouter createRouter(InstanceGetter inject) {
            TravelTime travelTime = inject.getModal(TravelTime.class);
            GlobalConfigGroup config = inject.get(GlobalConfigGroup.class);
            Network network = inject.getModal(Network.class);

            return new DefaultAStarLMRouter(DefaultParallelLeastCostPathCalculator.//
                    create(config.getNumberOfThreads(), new FastAStarLandmarksFactory(config), network, //
                            new OnlyTimeDependentTravelDisutilityFixed(travelTime), travelTime));
        }
    }
}
