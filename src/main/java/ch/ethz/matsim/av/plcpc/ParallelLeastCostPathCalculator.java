package ch.ethz.matsim.av.plcpc;

import java.io.Closeable;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.vehicles.Vehicle;

public interface ParallelLeastCostPathCalculator extends Closeable {
    Future<LeastCostPathCalculator.Path> calcLeastCostPath(Node fromNode, Node toNode, double starttime, final Person person, final Vehicle vehicle);

    // void update();
}
