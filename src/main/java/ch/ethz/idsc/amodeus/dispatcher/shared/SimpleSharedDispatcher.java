package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatchingUtils;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.HungarBiPartVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public class SimpleSharedDispatcher extends SharedUniversalDispatcher {

    private final int dispatchPeriod;
    private final DistanceHeuristics distanceHeuristics;
    private Tensor printVals = Tensors.empty();
    private final DistanceFunction distanceFunction;
    private final Network network;

    protected SimpleSharedDispatcher(Network network, //
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            EventsManager eventsManager) {
        super(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager);
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);
        distanceHeuristics = DistanceHeuristics.valueOf(safeConfig.getString("distanceHeuristics", // <- crashes if spelling is wrong
                DistanceHeuristics.EUCLIDEAN.name()).toUpperCase()); // TODO make EUCLIDEANNONCYCLIC default, also in the other dispatchers
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        this.distanceFunction = distanceHeuristics.getDistanceFunction(network);
        this.network = network;

    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            for (RoboTaxi sharedRoboTaxi : getDivertableUnassignedRoboTaxis()) {
                if (getUnassignedAVRequests().size() >= 4) {

                    AVRequest firstRequest = getUnassignedAVRequests().get(0);
                    AVRequest secondRequest = getUnassignedAVRequests().get(1);
                    AVRequest thirdRequest = getUnassignedAVRequests().get(2);
                    AVRequest fourthRequest = getUnassignedAVRequests().get(3);

                    addSharedRoboTaxiPickup(sharedRoboTaxi, firstRequest);

                    addSharedRoboTaxiPickup(sharedRoboTaxi, secondRequest);
                    SharedAVCourse sharedAVCourse = new SharedAVCourse(secondRequest.getId(), SharedAVMealType.PICKUP);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse);

                    addSharedRoboTaxiPickup(sharedRoboTaxi, thirdRequest);
                    SharedAVCourse sharedAVCourse3 = new SharedAVCourse(thirdRequest.getId(), SharedAVMealType.PICKUP);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);

                    addSharedRoboTaxiPickup(sharedRoboTaxi, fourthRequest);
                    SharedAVCourse sharedAVCourse4 = new SharedAVCourse(fourthRequest.getId(), SharedAVMealType.PICKUP);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);

                    // TODO CHECK the menu manipulation
                } else {
                    // TODO Improve and make function without break
                    break;
                }
            }
        }

    }

    public static class Factory implements AVDispatcherFactory {
        @Inject
        @Named(AVModule.AV_MODE)
        private ParallelLeastCostPathCalculator router;

        @Inject
        @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        @Inject(optional = true)
        private TravelData travelData;

        @Inject
        @Named(AVModule.AV_MODE)
        private Network network;

        @Inject
        private Config config;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig) {
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            AbstractVehicleDestMatcher abstractVehicleDestMatcher = new HungarBiPartVehicleDestMatcher(new EuclideanDistanceFunction());

            return new SimpleSharedDispatcher(network, config, avconfig, travelTime, router, eventsManager);
        }
    }

}
