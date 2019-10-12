/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import java.io.IOException;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.operator.RouterConfig;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.router.AVRouter;

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
        @Inject
        GlobalConfigGroup config;
        @Inject
        @Named(AVModule.AV_MODE)
        TravelTime travelTime;

        @Override
        public AVRouter createRouter(RouterConfig routerConfig, Network network) {
            return new DefaultAStarLMRouter(DefaultParallelLeastCostPathCalculator.//
                    create(config.getNumberOfThreads(), new FastAStarLandmarksFactory(), network, //
                            new OnlyTimeDependentTravelDisutilityFixed(travelTime), travelTime));

        }
    }
}
