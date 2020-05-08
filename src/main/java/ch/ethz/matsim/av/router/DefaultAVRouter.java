package ch.ethz.matsim.av.router;

import java.io.IOException;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** The DefaultAVRouter is a standard ParallelLeastCostPathCalculator using
 * Djikstra's algorithm. */
public class DefaultAVRouter implements AVRouter {
    public final static String TYPE = "Default";

    final private ParallelLeastCostPathCalculator delegate;

    DefaultAVRouter(ParallelLeastCostPathCalculator delegate) {
        this.delegate = delegate;
    }

    @Override
    public Future<Path> calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {
        return delegate.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public static class Factory implements AVRouter.Factory {
        @Override
        public AVRouter createRouter(InstanceGetter inject) {
            Network network = inject.getModal(Network.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            AmodeusConfigGroup config = inject.get(AmodeusConfigGroup.class);

            return new DefaultAVRouter(DefaultParallelLeastCostPathCalculator.create((int) config.getNumberOfParallelRouters(), new DijkstraFactory(), network,
                    new OnlyTimeDependentTravelDisutility(travelTime), travelTime));
        }
    }
}
