/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public class HeuristicSharedDispatcher extends SharedUniversalDispatcher {

    private static final double MAXSHAREDISTANCE = 2000;
    public final int dispatchPeriod;
    public final int rebalancingPeriod;
    private final Network network;
    Tensor printVals = Tensors.empty();
    TravelData travelData;

    protected HeuristicSharedDispatcher(Config config, //
            AVDispatcherConfig avconfig, //
            AVGeneratorConfig generatorConfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            Network network, //
            TravelData travelData) {
        super(config, avconfig, travelTime, router, eventsManager);
        this.travelData = travelData;
        this.network = network;
        SafeConfig safeConfig = SafeConfig.wrap(avconfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 600);
        rebalancingPeriod = safeConfig.getInteger("rebalancingPeriod", 30);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            List<AVRequest> unassignedRequests = getUnassignedAVRequests();
            Map<Link, Set<AVRequest>> unassignedFromLinks = getFromLinkMap(unassignedRequests);
            Set<AVRequest> assignements = new HashSet<>();
            Set<RoboTaxi> assignedRoboTaxis = new HashSet<>();
            for (AVRequest avRequest : unassignedRequests) {
                if (!assignements.contains(avRequest)) {

                    Set<Link> closeFromLinks = getCloseLinks(avRequest.getFromLink().getCoord(), MAXSHAREDISTANCE, network);

                    Set<AVRequest> potentialMatches = new HashSet<>();
                    for (Link fromLink : closeFromLinks) {
                        if (unassignedFromLinks.containsKey(fromLink)) {
                            for (AVRequest potentialMatch : unassignedFromLinks.get(fromLink)) {
                                if (!assignements.contains(potentialMatch)) {
                                    potentialMatches.add(potentialMatch);
                                }
                            }
                        }
                    }

                    List<AVRequest> matchesAV = new ArrayList<>();
                    Set<Link> closeToLinks = getCloseLinks(avRequest.getToLink().getCoord(), MAXSHAREDISTANCE, network);
                    for (AVRequest potentialMatch : potentialMatches) {
                        if (closeToLinks.contains(potentialMatch.getToLink())) {
                            matchesAV.add(potentialMatch);
                        }
                    }
                    if (matchesAV.contains(avRequest)) {
                        matchesAV.remove(avRequest);
                    }

                    Collection<RoboTaxi> roboTaxis = getDivertableUnassignedRoboTaxisWithoutCustomer();
                    if (!roboTaxis.isEmpty()) {
                        RoboTaxi matchedRoboTaxi = findClostestVehicle(avRequest, roboTaxis);
                        addSharedRoboTaxiPickup(matchedRoboTaxi, avRequest);
                        assignements.add(avRequest);
                        GlobalAssert.that(!assignedRoboTaxis.contains(matchedRoboTaxi));
                        assignedRoboTaxis.add(matchedRoboTaxi);
                        if (!matchesAV.isEmpty()) {
                            List<SharedAVCourse> pickupMenu = new ArrayList<>();
                            List<SharedAVCourse> dropoffMenu = new ArrayList<>();
                            int numberAssignements = Math.min(matchedRoboTaxi.getCapacity(), matchesAV.size() + 1);

                            List<AVRequest> sharingAssignments = matchesAV.subList(0, numberAssignements - 1);
                            for (AVRequest avRequestShared : sharingAssignments) {
                                addSharedRoboTaxiPickup(matchedRoboTaxi, avRequestShared);
                                assignements.add(avRequestShared);
                                pickupMenu.add(new SharedAVCourse(avRequestShared.getId(), SharedAVMealType.PICKUP));
                                dropoffMenu.add(new SharedAVCourse(avRequestShared.getId(), SharedAVMealType.DROPOFF));
                            }
                            SharedAVMenu sharedAVMenu = new SharedAVMenu();
                            sharedAVMenu.addAVCourseAsStarter(new SharedAVCourse(avRequest.getId(), SharedAVMealType.PICKUP));
                            pickupMenu.forEach(course -> sharedAVMenu.addAVCourseAsDessert(course));
                            sharedAVMenu.addAVCourseAsDessert(new SharedAVCourse(avRequest.getId(), SharedAVMealType.DROPOFF));
                            dropoffMenu.forEach(course -> sharedAVMenu.addAVCourseAsDessert(course));
                            GlobalAssert.that(sharedAVMenu.checkNoPickupAfterDropoffOfSameRequest());
                            matchedRoboTaxi.getMenu().replaceWith(sharedAVMenu);
                        }

                    }
                }

            }
        }
    }

    private static RoboTaxi findClostestVehicle(AVRequest avRequest, Collection<RoboTaxi> roboTaxis) {
        GlobalAssert.that(roboTaxis != null);
        RoboTaxi closestRoboTaxi = null;
        double closestRoboTaxidistance = 10000000000.0;
        for (RoboTaxi roboTaxi : roboTaxis) {
            if (closestRoboTaxi == null) {
                closestRoboTaxi = roboTaxi;
                closestRoboTaxidistance = distanceRobotaxiRequest(avRequest, roboTaxi);
            } else {
                double newDistance = distanceRobotaxiRequest(avRequest, roboTaxi);
                if (closestRoboTaxidistance > newDistance) {
                    closestRoboTaxi = roboTaxi;
                    closestRoboTaxidistance = newDistance;
                }
            }
        }
        return closestRoboTaxi;
    }

    private static double distanceRobotaxiRequest(AVRequest avRequest, RoboTaxi roboTaxi) {
        return NetworkUtils.getEuclideanDistance(avRequest.getFromLink().getCoord(), roboTaxi.getDivertableLocation().getCoord());
    }

    private static Set<Link> getCloseLinks(Coord coord, double distance, Network network) {
        Collection<Node> closeNodes = NetworkUtils.getNearestNodes(network, coord, distance);
        GlobalAssert.that(!closeNodes.isEmpty());
        Set<Link> closeLinks = new HashSet<>();
        for (Link link : network.getLinks().values()) {
            if (closeNodes.contains(link.getFromNode()) && closeNodes.contains(link.getToNode())) {
                closeLinks.add(link);
            }
        }
        GlobalAssert.that(!closeLinks.isEmpty());
        return closeLinks;
    }

    private static Map<Link, Set<AVRequest>> getFromLinkMap(Collection<AVRequest> avRequests) {
        Map<Link, Set<AVRequest>> linkAVRequestMap = new HashMap<>();
        for (AVRequest avRequest : avRequests) {
            Link fromLink = avRequest.getFromLink();
            if (!linkAVRequestMap.containsKey(fromLink)) {
                linkAVRequestMap.put(fromLink, new HashSet<>());
            }
            linkAVRequestMap.get(fromLink).add(avRequest);
        }
        return linkAVRequestMap;
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

            return new HeuristicSharedDispatcher(config, avconfig, generatorConfig, travelTime, router, eventsManager, network, travelData);
        }
    }

}
