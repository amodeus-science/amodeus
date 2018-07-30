/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedPartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatchingMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

public class SharedDispatcherExample extends SharedPartitionedDispatcher {

    private final int dispatchPeriod;
    private final int rebalancingPeriod;
    private final AbstractVirtualNodeDest virtualNodeDest;
    private final AbstractVehicleDestMatcher vehicleDestMatcher;
    private final int nVNodes;
    private final int nVLinks;
    private final Network network;
    private final DistanceFunction distanceFunction;
    private final DistanceHeuristics distanceHeuristics;
    private Tensor printVals = Tensors.empty();
    private TravelData travelData;

    protected SharedDispatcherExample(Config config, //
            AVDispatcherConfig avconfig, //
            AVGeneratorConfig generatorConfig, //
            TravelTime travelTime, //
            AVRouter router, //
            EventsManager eventsManager, //
            Network network, //
            VirtualNetwork<Link> virtualNetwork, //
            AbstractVirtualNodeDest abstractVirtualNodeDest, //
            AbstractVehicleDestMatcher abstractVehicleDestMatcher, //
            TravelData travelData) {
        super(config, avconfig, travelTime, router, eventsManager, virtualNetwork);
        virtualNodeDest = abstractVirtualNodeDest;
        vehicleDestMatcher = abstractVehicleDestMatcher;
        this.travelData = travelData;
        this.network = network;
        nVNodes = virtualNetwork.getvNodesCount();
        nVLinks = virtualNetwork.getvLinksCount();
        SafeConfig safeConfig = SafeConfig.wrap(avconfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 600);
        rebalancingPeriod = safeConfig.getInteger("rebalancingPeriod", 30);
        distanceHeuristics = DistanceHeuristics.valueOf(safeConfig.getString("distanceHeuristics", //
                DistanceHeuristics.EUCLIDEAN.name()).toUpperCase());
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        this.distanceFunction = distanceHeuristics.getDistanceFunction(network);

    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            if (getUnassignedAVRequests().size() >= 4) {

                Map<VirtualNode<Link>, List<RoboTaxi>> availableVehicles = getVirtualNodeDivertableUnassignedRoboTaxi();
                Map<VirtualNode<Link>, List<AVRequest>> availableRequests = getVirtualNodeRequests();

                for (Entry<VirtualNode<Link>, List<AVRequest>> entry : availableRequests.entrySet()) {

                    if (!entry.getValue().isEmpty()) {

                        int fixedCapacity = 4;

                        List<AVRequest> avRequests = entry.getValue();
                        Map<VirtualNode<Link>, List<AVRequest>> sameDestRequests = virtualNetwork.binToVirtualNode(avRequests, AVRequest::getToLink);
                        Map<VirtualNode<Link>, List<AVRequest>> shareableRequests = sameDestRequests.entrySet().stream().filter(e -> e.getValue().size() > 1)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        // Map<SharedRoboTaxi, List<AVRequest>> sharedAssignments = new HashMap<>();

                        for (List<AVRequest> shReqsList : shareableRequests.values()) {
                            int size = shReqsList.size();

                            int numberOfTaxis = (int) Math.ceil((double) size / (double) fixedCapacity);

                            availableVehicles = getVirtualNodeDivertableUnassignedRoboTaxi();

                            List<RoboTaxi> taxisToPair = new ArrayList<>();
                            if (availableVehicles.get(entry.getKey()).size() >= numberOfTaxis) {
                                taxisToPair = availableVehicles.get(entry.getKey()).subList(0, numberOfTaxis - 1);
                            } else {

                                if (availableVehicles.get(entry.getKey()).size() > 0) {
                                    taxisToPair = availableVehicles.get(entry.getKey()).subList(0, availableVehicles.get(entry.getKey()).size() - 1);
                                }

                                List<RoboTaxi> otherVlTaxis = new ArrayList<>();
                                availableVehicles.entrySet().stream().filter(e -> !e.getKey().equals(entry.getKey()))
                                        .forEach(vl -> vl.getValue().stream().forEach(r -> otherVlTaxis.add(r)));
                                taxisToPair.addAll(otherVlTaxis.subList(0, numberOfTaxis - taxisToPair.size() - 1));

                            }

                            for (int i = 0; i < numberOfTaxis; i++) {
                                List<AVRequest> subList = shReqsList.subList(i * fixedCapacity, Math.min((i + 1) * fixedCapacity, size));

                                RoboTaxi sRt = taxisToPair.get(i);
                                // Pair taxi to request
                                subList.stream().forEach(avr -> addSharedRoboTaxiPickup(sRt, avr));
                                // TODO SHARED reorder menu
                                SharedAVMenu menu = taxisToPair.get(i).getMenu();
                                @SuppressWarnings("unused")
                                List<Integer> pickupIndeces = menu.getPickupOrDropOffCoursesIndeces(SharedAVMealType.PICKUP);
                                // SharedAVCourse sharedAVCourse = new SharedAVCourse(secondRequest.getId(), SharedAVMealType.PICKUP);

                                // .moveAVCourseToPrev(sharedAVCourse);

                            }

                        }

                        // TODO SHARED GBPM
                        @SuppressWarnings("unused")
                        List<AVRequest> nonShareableOnes = sameDestRequests.values().stream() //
                                .filter(l -> l.size() <= 1).map(l -> l.get(0)) //
                                .collect(Collectors.toList());

                        availableVehicles = getVirtualNodeDivertableUnassignedRoboTaxi();

                    }
                }
                //
                // AVRequest firstRequest = getUnassignedAVRequests().get(0);
                // AVRequest secondRequest = getUnassignedAVRequests().get(1);
                // AVRequest thirdRequest = getUnassignedAVRequests().get(2);
                // AVRequest fourthRequest = getUnassignedAVRequests().get(3);
                //
                // addSharedRoboTaxiPickup(sharedRoboTaxi, firstRequest);
                //
                // addSharedRoboTaxiPickup(sharedRoboTaxi, secondRequest);
                // SharedAVCourse sharedAVCourse = new SharedAVCourse(secondRequest.getId(), SharedAVMealType.PICKUP);
                //
                // taxisToPair.get(i).getMenu().moveAVCourseToPrev(sharedAVCourse);
                //
                // addSharedRoboTaxiPickup(sharedRoboTaxi, thirdRequest);
                // SharedAVCourse sharedAVCourse3 = new SharedAVCourse(thirdRequest.getId(), SharedAVMealType.PICKUP);
                // sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);
                // sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse3);
                //
                // addSharedRoboTaxiPickup(sharedRoboTaxi, fourthRequest);
                // SharedAVCourse sharedAVCourse4 = new SharedAVCourse(fourthRequest.getId(), SharedAVMealType.PICKUP);
                // sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                // sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);
                // sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse4);

                // TODO SHARED the menu manipulation
            }
        }

    }

    private Map<VirtualNode<Link>, List<RoboTaxi>> getVirtualNodeDivertableUnassignedRoboTaxi() {
        return virtualNetwork.binToVirtualNode(getDivertableUnassignedRoboTaxis(), RoboTaxi::getDivertableLocation);
    }

    public static class Factory implements AVDispatcherFactory {
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

        @Inject(optional = true)
        private VirtualNetwork<Link> virtualNetwork;

        @Inject
        private Config config;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            AbstractVehicleDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatchingMatcher(new EuclideanDistanceFunction());

            return new SharedDispatcherExample(config, avconfig, generatorConfig, travelTime, router, eventsManager, network, virtualNetwork, abstractVirtualNodeDest,
                    abstractVehicleDestMatcher, travelData);
        }
    }

}
