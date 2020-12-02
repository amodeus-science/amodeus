/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.SharedRebalancingDispatcher;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.shared.Compatibility;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.util.math.GlobalAssert;

/** this is a demo of functionality for the shared dispatchers (> 1 person in {@link RoboTaxi}
 * 
 * whenever 4 {@link PassengerRequest}s are open, a {@link RoboTaxi} is assigned to pickup all of them,
 * it first picks up passengers 1,2,3,4 and then starts to bring passengers 1,2,3 to their destinations.
 * Passenger 4 is less lucky as the {@link RoboTaxi} first visits the city's North pole (northern most link)
 * before passenger 4 is finally dropped of and the procedure starts from beginning. */
public class NorthPoleSharedDispatcher extends SharedRebalancingDispatcher {
    private final int dispatchPeriod;
    private final int rebalancePeriod;
    private final Random randGen = new Random(1234);
    private final Link cityNorthPole;
    private final List<Link> equatorLinks;

    protected NorthPoleSharedDispatcher(Network network, //
            Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, db);
        this.cityNorthPole = getNorthPole(network);
        this.equatorLinks = getEquator(network);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        rebalancePeriod = dispatcherConfig.getRebalancingPeriod(1800);
        Collections.shuffle(new ArrayList<>(network.getLinks().values()), randGen);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0)
            /** assignment of {@link RoboTaxi}s */
            for (RoboTaxi sharedRoboTaxi : getDivertableUnassignedRoboTaxis())
                if (getUnassignedPassengerRequests().size() >= 4) {
                    /** select 4 requests */
                    PassengerRequest firstRequest = getUnassignedPassengerRequests().get(0);
                    PassengerRequest secondRequest = getUnassignedPassengerRequests().get(1);
                    PassengerRequest thirdRequest = getUnassignedPassengerRequests().get(2);
                    PassengerRequest fourthRequest = getUnassignedPassengerRequests().get(3);

                    /** add pickup for request 1 */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, firstRequest);

                    /** add pickup for request 2 and move to first location */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, secondRequest);
                    Directive sharedAVCourse = Directive.pickup(secondRequest);
                    sharedRoboTaxi.moveToPrevious(sharedAVCourse);

                    /** add pickup for request 3 and move to first location */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, thirdRequest);
                    Directive sharedAVCourse3 = Directive.pickup(thirdRequest);
                    sharedRoboTaxi.moveToPrevious(sharedAVCourse3);
                    sharedRoboTaxi.moveToPrevious(sharedAVCourse3);

                    /** add pickup for request 4 and reorder the menu based on a list of Shared Courses */
                    List<Directive> courses = new ArrayList<>(sharedRoboTaxi.getUnmodifiableViewOfCourses());
                    courses.add(3, Directive.pickup(fourthRequest));
                    courses.add(Directive.dropoff(fourthRequest));
                    addSharedRoboTaxiPickup(sharedRoboTaxi, fourthRequest);
                    sharedRoboTaxi.updateMenu(courses);

                    /** add a redirect task (to the north pole) and move to prev */
                    Directive redirectCourse = Directive.drive(cityNorthPole);
                    addSharedRoboTaxiRedirect(sharedRoboTaxi, redirectCourse);
                    sharedRoboTaxi.moveToPrevious(redirectCourse);

                    /** check consistency and end */
                    GlobalAssert.that(Compatibility.of(sharedRoboTaxi.getUnmodifiableViewOfCourses()).forCapacity(sharedRoboTaxi.getScheduleManager(), sharedRoboTaxi.getCapacity()));
                } else
                    break;

        /** dispatching of available {@link RoboTaxi}s to the equator */
        if (round_now % rebalancePeriod == 0)
            /** relocation of empty {@link RoboTaxi}s to a random link on the equator */
            for (RoboTaxi roboTaxi : getDivertableUnassignedRoboTaxis()) {
                Link rebalanceLink = equatorLinks.get(randGen.nextInt(equatorLinks.size()));
                setRoboTaxiRebalance(roboTaxi, rebalanceLink);
            }
    }

    /** @param network
     * @return northern most {@link Link} in the {@link Network} */
    private static Link getNorthPole(Network network) {
        return network.getLinks().values().stream().max(Comparator.comparingDouble(l -> l.getCoord().getY())).get();
    }

    /** @param network
     * @return all {@link Link}s crossing the equator of the city {@link Network} , starting
     *         with links on the equator, if no links found, the search radius is increased by 1 m */
    private static List<Link> getEquator(Network network) {
        double northX = network.getLinks().values().stream().map(Link::getCoord).map(Coord::getY).max(Double::compare).get();
        double southX = network.getLinks().values().stream().map(Link::getCoord).map(Coord::getY).min(Double::compare).get();
        double equator = southX + (northX - southX) / 2;

        List<Link> equatorLinks = new ArrayList<>();
        double margin = 0.0;
        while (equatorLinks.isEmpty()) {
            for (Link l : network.getLinks().values()) {
                double fromY = l.getFromNode().getCoord().getY();
                double toY = l.getToNode().getCoord().getY();
                if ((fromY - margin <= equator && toY + margin >= equator) || //
                        (fromY + margin >= equator && toY - margin <= equator))
                    equatorLinks.add(l);
            }
            margin += 1.0;
        }
        // GlobalAssert.that(equatorLinks.size() > 0); // always true
        return equatorLinks;
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            return new NorthPoleSharedDispatcher(network, config, operatorConfig, travelTime, router, eventsManager, db);
        }
    }
}
