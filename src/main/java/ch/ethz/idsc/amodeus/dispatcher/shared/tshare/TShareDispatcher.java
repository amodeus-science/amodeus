/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiUtils;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedPartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinDistDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

public class TShareDispatcher extends SharedPartitionedDispatcher {

    private final int dispatchPeriod;
    private final int rebalancePeriod;
    private final List<Link> links;
    private final Random randGen = new Random(1234);
    private final Link cityNorthPole;
    private final List<Link> equatorLinks;
    private final Map<VirtualNode<Link>, GridCell> gridCells = new HashMap<>();
    private final double delayMax;

    protected TShareDispatcher(Network network, //
            Config config, AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, //
            VirtualNetwork<Link> virtualNetwork) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, virtualNetwork, db);
        /** parameters */
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);
        rebalancePeriod = safeConfig.getInteger("rebalancingPeriod", 1800);
        delayMax = safeConfig.getInteger("delayMax", 10 * 60);
        
        /** initialize grid with T-cells */
        NetworkDistanceFunction minDist = new NetworkMinDistDistanceFunction(network, new FastAStarLandmarksFactory());
        NetworkDistanceFunction minTime = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
        QuadTree<Link> linkTree = FastQuadTree.of(network);
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes()) {
            gridCells.put(virtualNode, new GridCell(virtualNode, virtualNetwork, network, minDist, minTime, linkTree));
        }

        System.exit(1);

        this.cityNorthPole = getNorthPole(network);
        this.equatorLinks = getEquator(network);
        links = new ArrayList<>(network.getLinks().values());
        Collections.shuffle(links, randGen);
        
        
        // TODO ensure that rectangular grid was used
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            /** assignment of {@link RoboTaxi}s */
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
                    sharedRoboTaxi.moveAVCourseToPrev(sharedAVCourse);

                    /** add pickup for request 3 and move to first location */
                    addSharedRoboTaxiPickup(sharedRoboTaxi, thirdRequest);
                    SharedCourse sharedAVCourse3 = SharedCourse.pickupCourse(thirdRequest);
                    sharedRoboTaxi.moveAVCourseToPrev(sharedAVCourse3);
                    sharedRoboTaxi.moveAVCourseToPrev(sharedAVCourse3);

                    /** add pickup for request 4 and reorder the menu based on a list of Shared Courses */
                    List<SharedCourse> courses = SharedCourseListUtils.copy(sharedRoboTaxi.getUnmodifiableViewOfCourses());
                    courses.add(3, SharedCourse.pickupCourse(fourthRequest));
                    courses.add(SharedCourse.dropoffCourse(fourthRequest));
                    addSharedRoboTaxiPickup(sharedRoboTaxi, fourthRequest);
                    sharedRoboTaxi.updateMenu(courses);

                    /** add a redirect task (to the north pole) and move to prev */
                    Link redirectLink = cityNorthPole;
                    SharedCourse redirectCourse = SharedCourse.redirectCourse(redirectLink, Double.toString(now) + sharedRoboTaxi.getId().toString());
                    addSharedRoboTaxiRedirect(sharedRoboTaxi, redirectCourse);
                    sharedRoboTaxi.moveAVCourseToPrev(redirectCourse);

                    /** check consistency and end */
                    GlobalAssert.that(RoboTaxiUtils.checkMenuConsistency(sharedRoboTaxi));
                } else {
                    break;
                }
            }
        }

        /** dispatching of available {@link RoboTaxi}s to the equator */
        if (round_now % rebalancePeriod == 0) {
            /** relocation of empty {@link RoboTaxi}s to a random link on the equator */
            for (RoboTaxi roboTaxi : getDivertableUnassignedRoboTaxis()) {
                Link rebalanceLink = equatorLinks.get(randGen.nextInt(equatorLinks.size()));
                setRoboTaxiRebalance(roboTaxi, rebalanceLink);
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

    /** @param network
     * @return all {@link Link}s crossing the equator of the city {@link Network} , starting
     *         with links on the equator, if no links found, the search radius is increased by 1 m */
    private static List<Link> getEquator(Network network) {
        NavigableMap<Double, Link> links = new TreeMap<>();
        network.getLinks().values().stream().forEach(l -> {
            links.put(l.getCoord().getY(), l);
        });
        double northX = links.lastEntry().getValue().getCoord().getY();
        double southX = links.firstEntry().getValue().getCoord().getY();
        double equator = southX + (northX - southX) / 2;

        List<Link> equatorLinks = new ArrayList<>();
        double margin = 0.0;
        while (equatorLinks.size() < 1) {
            for (Link l : network.getLinks().values()) {
                boolean crossEq1 = l.getFromNode().getCoord().getY() - margin <= //
                equator && l.getToNode().getCoord().getY() + margin >= equator;
                boolean crossEq2 = l.getFromNode().getCoord().getY() + margin >= //
                equator && l.getToNode().getCoord().getY() - margin <= equator;
                if (crossEq1 || crossEq2)
                    equatorLinks.add(l);
            }
            margin += 1.0;
        }
        GlobalAssert.that(equatorLinks.size() > 0);
        return equatorLinks;
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

        @Inject
        private MatsimAmodeusDatabase db;

        @Inject
        private VirtualNetwork<Link> virtualNetwork;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            @SuppressWarnings("unused")
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            @SuppressWarnings("unused")
            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            @SuppressWarnings("unused")
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceFunction.INSTANCE);

            return new TShareDispatcher(network, config, avconfig, travelTime, router, eventsManager, db, virtualNetwork);
        }
    }
}
