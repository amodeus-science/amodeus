package ch.ethz.matsim.av.plcpc;

import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.vehicles.Vehicle;

public class SerialLeastCostPathCalculator implements ParallelLeastCostPathCalculator {
    final private LeastCostPathCalculator delegate;

    public SerialLeastCostPathCalculator(LeastCostPathCalculator delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() {
    }

    @Override
    public Future<Path> calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {
        return ConcurrentUtils.constantFuture(delegate.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle));
    }

    /* @Override
     * public void update() {
     * } */
}
