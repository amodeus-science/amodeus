/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** @author Nicolo Ormezzano, Lukas Sieber */
public class SimpleSharedDispatcher extends SharedUniversalDispatcher {

    private final int dispatchPeriod;
    // private Tensor printVals = Tensors.empty();

    protected SimpleSharedDispatcher(Network network, //
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, //
            AVRouter router, //
            EventsManager eventsManager) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager);
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);
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
                    SharedRoboTaxiCourse sharedAVCourse = new SharedRoboTaxiCourse(secondRequest.getId(), SharedRoboTaxiMealType.PICKUP);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse);

                    addSharedRoboTaxiPickup(sharedRoboTaxi, thirdRequest);
                    SharedRoboTaxiCourse sharedAVCourse3 = new SharedRoboTaxiCourse(thirdRequest.getId(), SharedRoboTaxiMealType.PICKUP);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);

                    addSharedRoboTaxiPickup(sharedRoboTaxi, fourthRequest);
                    SharedRoboTaxiCourse sharedAVCourse4 = new SharedRoboTaxiCourse(fourthRequest.getId(), SharedRoboTaxiMealType.PICKUP);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);

                    sharedRoboTaxi.checkMenuConsistency();
                } else {
                    break;
                }
            }
        }

    }

    public static class Factory implements AVDispatcherFactory {
        @Inject
        @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        // @Inject(optional = true)
        // private TravelData travelData;

        @Inject
        @Named(AVModule.AV_MODE)
        private Network network;

        @Inject
        private Config config;

        @Override

        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            // TODO SHARED unfinished
            @SuppressWarnings("unused")
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            @SuppressWarnings("unused")
            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            @SuppressWarnings("unused")
            AbstractVehicleDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(new EuclideanDistanceFunction());

            return new SimpleSharedDispatcher(network, config, avconfig, travelTime, router, eventsManager);
        }
    }

}
