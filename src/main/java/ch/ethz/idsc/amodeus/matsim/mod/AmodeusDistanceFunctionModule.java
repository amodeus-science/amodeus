package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.AStarEuclideanDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.AStarLandmarksDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.DijkstraDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.DistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.EuclideanDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;

public class AmodeusDistanceFunctionModule extends AbstractModule {
    final private SafeConfig safeConfig;

    public AmodeusDistanceFunctionModule(SafeConfig safeConfig) {
        this.safeConfig = safeConfig;
    }

    @Override
    public void install() {
        String distanceFunction = safeConfig.getString("distance_function", "euclidean");

        switch (distanceFunction) {
        case "euclidean":
            bind(DistanceFunctionFactory.class).to(EuclideanDistanceFunctionFactory.class);
            break;
        case "dijkstra":
            bind(DistanceFunctionFactory.class).to(DijkstraDistanceFunctionFactory.class);
            break;
        case "astar_euclidean":
            bind(DistanceFunctionFactory.class).to(AStarEuclideanDistanceFunctionFactory.class);
            break;
        case "astar_landmarks":
            bind(DistanceFunctionFactory.class).to(AStarLandmarksDistanceFunctionFactory.class);
            break;
        default:
            throw new IllegalArgumentException("Unknown distance function: " + distanceFunction);
        }
    }

    @Provides
    @Singleton
    public EuclideanDistanceFunctionFactory provideEulideanDistanceFunctionFactory() {
        return new EuclideanDistanceFunctionFactory();
    }

    @Provides
    @Singleton
    public DijkstraDistanceFunctionFactory provideDijkstraDistanceFunctionFactory() {
        return new DijkstraDistanceFunctionFactory();
    }

    @Provides
    @Singleton
    public AStarEuclideanDistanceFunctionFactory provideAStarEuclideanDistanceFunctionFactory() {
        double overdoFactor = safeConfig.getDouble("astar_overdo_factor", 1.0);
        return new AStarEuclideanDistanceFunctionFactory(overdoFactor);
    }

    @Provides
    @Singleton
    public AStarLandmarksDistanceFunctionFactory provideDistanceFunctionFactory() {
        double overdoFactor = safeConfig.getDouble("astar_overdo_factor", 1.0);
        int numberOfLandmarks = safeConfig.getIntegerStrict("astar_number_of_landmarks");

        return new AStarLandmarksDistanceFunctionFactory(numberOfLandmarks, overdoFactor);
    }
}
