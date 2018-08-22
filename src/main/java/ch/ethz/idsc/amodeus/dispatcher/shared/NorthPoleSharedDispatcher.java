/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedRebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
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

/** this is a demo of functionality for the shared dispatchers (> 1 person in {@link RoboTaxi}
 * 
 * whenever 4 {@link AVRequest}s are open, a {@link RoboTaxi} is assigned to pickup all of them,
 * it first picks up passengers 1,2,3,4 and then starts to bring passengers 1,2,3 to their destinations.
 * Passenger 4 is less lucky as the {@link RoboTaxi} first visits the city's North pole (northern most link)
 * before passenger 4 is finally dropped of and the procedure starts from beginning. */
public class NorthPoleSharedDispatcher extends SharedRebalancingDispatcher {

    private final int dispatchPeriod;
    private final List<Link> links;
    private final Random randGen = new Random(1234);
    private final Link cityNorthPole;

    protected NorthPoleSharedDispatcher(Network network, //
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, //
            AVRouter router, //
            EventsManager eventsManager) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager);
        this.cityNorthPole = getNorthPole(network);
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

                    /** add a redirect task (to the north pole) and move to prev */
                    Link redirectLink = cityNorthPole;
                    SharedCourse redirectCourse = SharedCourse.redirectCourse(redirectLink, //
                            Double.toString(now) + sharedRoboTaxi.getId().toString());
                    addSharedRoboTaxiRedirect(sharedRoboTaxi, redirectCourse);
                    sharedRoboTaxi.getMenu().moveAVCourseToPrev(redirectCourse);

                    /** check consistency and end */
                    sharedRoboTaxi.checkMenuConsistency();
                } else {
                    break;
                }
            }
        }

    }

    /** @param network
     * @return northern most {@link Link} in the {@link Network} */
    private static Link getNorthPole(Network network) {
        NavigableMap<Double, Link> links = new TreeMap<>();
        network.getLinks().values().stream().forEach(l -> {
            links.put(l.getCoord().getY(), l);
        });
        return links.lastEntry().getValue();
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
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(new EuclideanDistanceFunction());

            return new NorthPoleSharedDispatcher(network, config, avconfig, travelTime, router, eventsManager);
        }
    }

}
