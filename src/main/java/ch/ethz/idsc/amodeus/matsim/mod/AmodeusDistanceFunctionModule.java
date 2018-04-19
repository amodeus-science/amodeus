package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.AStarEuclideanDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.AStarLandmarksDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.DijkstraDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.DistanceFunctionFactory;
import ch.ethz.idsc.amodeus.dispatcher.util.distance_function.EuclideanDistanceFunctionFactory;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;

public class AmodeusDistanceFunctionModule extends AbstractModule {
    final private ScenarioOptions scenarioOptions;

    public AmodeusDistanceFunctionModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        MapBinder<String, DistanceFunctionFactory> dfBinder = MapBinder.newMapBinder(binder(), String.class, DistanceFunctionFactory.class);

        dfBinder.addBinding("euclidean").to(EuclideanDistanceFunctionFactory.class);
        dfBinder.addBinding("dijkstra").to(DijkstraDistanceFunctionFactory.class);
        dfBinder.addBinding("astar_euclidean").to(AStarEuclideanDistanceFunctionFactory.class);
        dfBinder.addBinding("astar_landmarks").to(AStarLandmarksDistanceFunctionFactory.class);
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
        double overdoFactor = scenarioOptions.getAStarOverdoFactor();
        return new AStarEuclideanDistanceFunctionFactory(overdoFactor);
    }

    @Provides
    @Singleton
    public AStarLandmarksDistanceFunctionFactory provideDistanceFunctionFactory() {
        double overdoFactor = scenarioOptions.getAStarOverdoFactor();
        int numberOfLandmarks = scenarioOptions.getNumberOfAStarLandmarks();

        return new AStarLandmarksDistanceFunctionFactory(numberOfLandmarks, overdoFactor);
    }
}
