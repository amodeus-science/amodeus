/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.router.AVRouter;

/** This is a nonfunctional sample demonstrating of how to include a custom
 * router to AMoDeus which is not the standard choice of the Paralllel Djikstra
 * router used normally to calculate the path for {@link RoboTaxi} */
public class TaxiTravelTimeRouter implements AVRouter {
    private final ParallelLeastCostPathCalculator delegate;

    public TaxiTravelTimeRouter(ParallelLeastCostPathCalculator delegate) {
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
        // // FIXME @sebhoerl get from Inject as above...
        File workingDir = MultiFileTools.getDefaultWorkingDirectory();
        File linkSpeedDataFile = new File(workingDir, "linkSpeedData");
        LinkSpeedDataContainer lsData = LinkSpeedUtils.loadLinkSpeedData(linkSpeedDataFile);

        @Override
        public AVRouter createRouter(InstanceGetter inject) {
            GlobalConfigGroup config = inject.get(GlobalConfigGroup.class);
            Network network = inject.getModal(Network.class);

            TravelTime travelTime = new LSDataTravelTime(lsData);

            return new TaxiTravelTimeRouter(DefaultParallelLeastCostPathCalculator.create(config.getNumberOfThreads(), //
                    new DijkstraFactory(), network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime));
        }
    }
}
