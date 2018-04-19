package ch.ethz.idsc.amodeus.dispatcher.util.distance_function;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.ArrayFastRouterDelegateFactory;
import org.matsim.core.router.FastAStarEuclidean;
import org.matsim.core.router.FastRouterDelegateFactory;
import org.matsim.core.router.util.ArrayRoutingNetworkFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.PreProcessEuclidean;
import org.matsim.core.router.util.PreProcessLandmarks;
import org.matsim.core.router.util.RoutingNetwork;
import org.matsim.core.router.util.RoutingNetworkFactory;
import org.matsim.core.router.util.RoutingNetworkNode;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class AStarEuclideanFactory implements LeastCostPathCalculatorFactory {
    final private RoutingNetworkFactory routingNetworkFactory = new ArrayRoutingNetworkFactory();
    final private double overdoFactor;

    private Network network;

    private RoutingNetwork routingNetwork;
    private PreProcessEuclidean preProcessEucledian;

    public AStarEuclideanFactory(double overdofactor) {
        this.overdoFactor = overdofactor;
    }

    @Override
    public synchronized LeastCostPathCalculator createPathCalculator(Network network, TravelDisutility travelCosts, TravelTime travelTimes) {
        if (this.network == null) {
            this.network = network;

            routingNetwork = routingNetworkFactory.createRoutingNetwork(network);

            TravelDisutility disutility = new TravelDisutility() {
                @Override
                public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
                    return travelTimes.getLinkTravelTime(link, 0.0, person, vehicle);
                }

                @Override
                public double getLinkMinimumTravelDisutility(Link link) {
                    return travelTimes.getLinkTravelTime(link, 0.0, null, null);
                }
            };

            preProcessEucledian = new PreProcessEuclidean(disutility);
            preProcessEucledian.run(network);

            for (RoutingNetworkNode node : routingNetwork.getNodes().values()) {
                node.setDeadEndData(preProcessEucledian.getNodeData(node.getNode()));
            }
        } else if (!this.network.equals(network)) {
            throw new IllegalStateException();
        }

        FastRouterDelegateFactory fastRouterFactory = new ArrayFastRouterDelegateFactory();

        try {
            Constructor<FastAStarEuclidean> constructor = FastAStarEuclidean.class.getDeclaredConstructor(RoutingNetwork.class, PreProcessEuclidean.class, TravelDisutility.class,
                    TravelTime.class, double.class, FastRouterDelegateFactory.class);

            constructor.setAccessible(true);
            return constructor.newInstance(routingNetwork, preProcessEucledian, travelCosts, travelTimes, overdoFactor, fastRouterFactory);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}