/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfig;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

// TODO Lukas if some value for explaining API, move to NorthPoledispatcher, otherwise delete.
/** simple demonstration of shared {@link RoboTaxi} dispatching and rebalancing functionality */
public class SharedHeuristicDispatcher extends SharedUniversalDispatcher {

    private final double shareDistMax;
    private final int dispatchPeriod;
    private final Network network;
    private final double[] networkBounds;

    protected SharedHeuristicDispatcher(Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            AVGeneratorConfig generatorConfig, //
            TravelTime travelTime, //
            AVRouter router, //
            EventsManager eventsManager, //
            Network network, //
            MatsimAmodeusDatabase db) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, db);
        this.network = network;
        this.networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(600);
        shareDistMax = dispatcherConfig.getInteger("sharingDistanceMaximum", 2000);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            List<AVRequest> unassgndRqsts = getUnassignedAVRequests();
            Set<AVRequest> assignements = new HashSet<>();
            Set<RoboTaxi> assignedRoboTaxis = new HashSet<>();
            Map<Link, Set<AVRequest>> unassgndFrLnks = StaticHelper.getFromLinkMap(unassgndRqsts);
            for (AVRequest avRequest : unassgndRqsts) {
                if (!assignements.contains(avRequest)) {
                    Set<Link> closeFromLinks = StaticHelper.getCloseLinks(avRequest.getFromLink().getCoord(), shareDistMax, network);
                    Set<AVRequest> potentialMatches = new HashSet<>();
                    for (Link fromLink : closeFromLinks) {
                        if (unassgndFrLnks.containsKey(fromLink)) {
                            for (AVRequest potentialMatch : unassgndFrLnks.get(fromLink)) {
                                if (!assignements.contains(potentialMatch)) {
                                    potentialMatches.add(potentialMatch);
                                }
                            }
                        }
                    }

                    List<AVRequest> matchesAV = new ArrayList<>();
                    Set<Link> closeToLinks = StaticHelper.getCloseLinks(avRequest.getToLink().getCoord(), shareDistMax, network);
                    for (AVRequest potentialMatch : potentialMatches) {
                        if (closeToLinks.contains(potentialMatch.getToLink())) {
                            matchesAV.add(potentialMatch);
                        }
                    }
                    if (matchesAV.contains(avRequest)) {
                        matchesAV.remove(avRequest);
                    }

                    Collection<RoboTaxi> roboTaxis = getDivertableUnassignedRoboTaxis();
                    if (!roboTaxis.isEmpty()) {
                        RoboTaxi matchedRoboTaxi = StaticHelper.findClostestVehicle(avRequest, roboTaxis, networkBounds);
                        addSharedRoboTaxiPickup(matchedRoboTaxi, avRequest);
                        assignements.add(avRequest);
                        GlobalAssert.that(!assignedRoboTaxis.contains(matchedRoboTaxi));
                        assignedRoboTaxis.add(matchedRoboTaxi);
                        if (!matchesAV.isEmpty()) {
                            List<SharedCourse> pickupMenu = new ArrayList<>();
                            List<SharedCourse> dropoffMenu = new ArrayList<>();
                            int numberAssignements = Math.min(matchedRoboTaxi.getCapacity(), matchesAV.size() + 1);

                            List<AVRequest> sharingAssignments = matchesAV.subList(0, numberAssignements - 1);
                            for (AVRequest avReqShrd : sharingAssignments) {
                                addSharedRoboTaxiPickup(matchedRoboTaxi, avReqShrd);
                                assignements.add(avReqShrd);
                                pickupMenu.add(SharedCourse.pickupCourse(avReqShrd));
                                dropoffMenu.add(SharedCourse.dropoffCourse(avReqShrd));
                            }
                            List<SharedCourse> list = new ArrayList<>();
                            list.add(SharedCourse.dropoffCourse(avRequest));
                            list.addAll(pickupMenu);
                            list.add(SharedCourse.dropoffCourse(avRequest));
                            list.addAll(dropoffMenu);
                            matchedRoboTaxi.updateMenu(list);
                        }
                    }
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

        @Inject
        @Named(AVModule.AV_MODE)
        private Network network;

        @Inject
        private Config config;

        @Inject
        private MatsimAmodeusDatabase db;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();
            return new SharedHeuristicDispatcher(config, avconfig, generatorConfig, travelTime, router, eventsManager, network, db);
        }
    }

}
