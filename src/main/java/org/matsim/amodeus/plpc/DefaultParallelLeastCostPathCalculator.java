package org.matsim.amodeus.plpc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class DefaultParallelLeastCostPathCalculator implements ParallelLeastCostPathCalculator {
    final private BlockingQueue<LeastCostPathCalculator> calculators = new LinkedBlockingQueue<>();
    final private ExecutorService executor;

    public DefaultParallelLeastCostPathCalculator(Collection<LeastCostPathCalculator> calculators) {
        this.calculators.addAll(calculators);
        this.executor = Executors.newFixedThreadPool(calculators.size());
    }

    @Override
    public Future<Path> calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {
        Future<Path> future = executor.submit(() -> {
            LeastCostPathCalculator calculator = calculators.take();
            Path path = calculator.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);
            calculators.put(calculator);
            return path;
        });

        // futures.add(future);
        return future;
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    static public ParallelLeastCostPathCalculator create(int numberOfInstances, LeastCostPathCalculatorFactory factory, Network network, TravelDisutility travelDisutility,
            TravelTime travelTime) {
        List<LeastCostPathCalculator> instances = new LinkedList<>();

        for (int i = 0; i < numberOfInstances; i++) {
            instances.add(factory.createPathCalculator(network, travelDisutility, travelTime));
        }

        return new DefaultParallelLeastCostPathCalculator(instances);

        // return new SerialLeastCostPathCalculator(factory.createPathCalculator(network, travelDisutility, travelTime));

    }
}
