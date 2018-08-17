/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedRebalancingDispatcher;
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

/** demo of functionality for the shared dispatchers (> 1 person in {@link RoboTaxi}
 * 
 * at the dispatch period, the dispatcher iterates on all empty
 * {@link RoboTaxi}s and assigns the first 4 unassigned {@link AVRequest}s to them,
 * these requests are at arbitrary locations in the city, between the third and
 * the fourth request a random redirect location is chosen */
public class SimpleSharedDispatcher extends SharedRebalancingDispatcher {

    private final int dispatchPeriod;
    private final List<Link> links;
    private final Random randGen = new Random(1234);

    protected SimpleSharedDispatcher(Network network, //
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, //
            AVRouter router, //
            EventsManager eventsManager) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager);
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);
        links = new ArrayList<>(network.getLinks().values());
        Collections.shuffle(links, randGen);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            for (RoboTaxi sharedRoboTaxi : getDivertableUnassignedRoboTaxis()) {
                if (getUnassignedAVRequests().size() >= 4) {

                    /** select 4 requests */
                    AVRequest firstRequest = getUnassignedAVRequests().get(0);
                    AVRequest secondRequest = getUnassignedAVRequests().get(1);
                    AVRequest thirdRequest = getUnassignedAVRequests().get(2);
                    AVRequest fourthRequest = getUnassignedAVRequests().get(3);

                    /** add pickup for request 1 */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, firstRequest);

                    /** add pickup for request 2 and move to first location */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, secondRequest);
                    SharedCourse sharedAVCourse = SharedCourse.pickupCourse(secondRequest);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse);

                    /** add pickup for request 3 and move to first location */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, thirdRequest);
                    SharedCourse sharedAVCourse3 = SharedCourse.pickupCourse(thirdRequest);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);

                    /** add pickup for request 4 and move to first location */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, fourthRequest);
                    SharedCourse sharedAVCourse4 = SharedCourse.pickupCourse(fourthRequest);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);

                    /** add a redirect task and move to prev */
                    Link redirectLink = pollNextDestination();
                    SharedCourse redirectCourse = SharedCourse.redirectCourse(redirectLink, //
                            Double.toString(now) + sharedRoboTaxi.getId().toString());
                    addSharedRoboTaxiRedirect(sharedRoboTaxi,redirectCourse);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(redirectCourse);

                    /** check consistency and end */
                    sharedRoboTaxi.checkMenuConsistency();
                } else {
                    break;
                }
            }
        }

    }

    private Link pollNextDestination() {
        int index = randGen.nextInt(links.size());
        Link link = links.get(index);
        return link;
    }

    public static class Factory implements AVDispatcherFactory {
        @Inject
        @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        @Inject
        @Named(AVModule.AV_MODE)
        private Network network;

        @Inject
        private Config config;

        @Override

        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
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
