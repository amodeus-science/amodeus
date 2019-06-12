/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfig;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedRebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.parking.strategies.ParkingStrategy;
import ch.ethz.idsc.amodeus.dispatcher.shared.basic.ExtDemandSupplyBeamSharing;
import ch.ethz.idsc.amodeus.dispatcher.shared.beam.BeamExtensionForSharing;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceCost;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMaintainer;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.opt.Pi;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** This is a first Dispatcher which takes the Parking Situation into Account.
 * 
 * To run this dipatcher it is required that
 * 1. The Matsim Controler (e.g. in the ScenarioServer) uses the {@link AmodeusParkingModule}.
 * 2. Set the values for the {@link ParkingCapacityAmodeus} and the {@link ParkingStrategy} in the Scenario Options
 * 3. Choose the {@link RestrictedLinkCapacityDispatcher} in the AVConfig.xml
 * 
 * It extends the {@link ExtDemandSupplyBeamSharing}. At each pickup it is
 * checked if around this Robotaxi there exist other Open requests with the same
 * direction. Those are then picked up. */
public class RestrictedLinkCapacityDispatcher extends SharedRebalancingDispatcher {

    private final int dispatchPeriod;

    /** ride sharing parameters */
    /** the sharing period says every how many seconds the dispatcher should chekc if
     * new pickups occured */
    private final int sharingPeriod; // [s]
    private final BeamExtensionForSharing beamExtensionForSharing;

    /** PARKING EXTENSION */
    private final ParkingStrategy parkingStrategy;
    /** PARKING EXTENSION */

    /** the maximal angle between the two directions which is allowed that sharing
     * occurs */

    /** data structures are used to enable fast "contains" searching */
    private final TreeMaintainer<AVRequest> requestMaintainer;
    private final double[] networkBounds;

    protected RestrictedLinkCapacityDispatcher(Network network, //
            Config config, AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, ParkingStrategy parkingStrategy, //
            ParkingCapacityAmodeus avSpatialCapacityAmodeus) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, db);
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 60);
        sharingPeriod = safeConfig.getInteger("sharingPeriod", 10); // makes sense to choose this value similar to the
                                                                    // pickup duration
        double rMax = safeConfig.getDouble("rMax", 1000.0);
        double phiMax = Pi.in(100).multiply(RealScalar.of(safeConfig.getDouble("phiMaxDeg", 5.0) / 180.0)).number().doubleValue();
        beamExtensionForSharing = new BeamExtensionForSharing(rMax, phiMax);
        this.networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        this.requestMaintainer = new TreeMaintainer<>(networkBounds, this::getLocation);

        /** PARKING EXTENSION */
        this.parkingStrategy = parkingStrategy;
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        DistanceHeuristics distanceHeuristics = //
                dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.ASTARLANDMARKS);
        this.parkingStrategy.setRunntimeParameters(avSpatialCapacityAmodeus, network, distanceHeuristics.getDistanceFunction(network));
        /** PARKING EXTENSION */
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            // STANDARD DEMAND SUPPLY IMPLEMENTATION
            /** get open requests and available vehicles */
            Collection<RoboTaxi> robotaxisDivertable = getDivertableUnassignedRoboTaxis();
            TreeMaintainer<RoboTaxi> unassignedRoboTaxis = new TreeMaintainer<>(networkBounds, this::getRoboTaxiLoc);

            robotaxisDivertable.stream().forEach(rt -> unassignedRoboTaxis.add(rt));

            List<AVRequest> requests = getUnassignedAVRequests();
            requests.stream().forEach(r -> requestMaintainer.add(r));

            /** distinguish over- and undersupply cases */
            boolean oversupply = false;
            if (unassignedRoboTaxis.size() >= requests.size())
                oversupply = true;

            if (unassignedRoboTaxis.size() > 0 && requests.size() > 0) {
                /** oversupply case */
                if (oversupply) {
                    for (AVRequest avr : requests) {
                        RoboTaxi closest = unassignedRoboTaxis.getClosest(getLocation(avr));
                        if (closest != null) {
                            addSharedRoboTaxiPickup(closest, avr);

                            unassignedRoboTaxis.remove(closest);
                            requestMaintainer.remove(avr);
                        }
                    }
                    /** undersupply case */
                } else {
                    for (RoboTaxi robotaxi : robotaxisDivertable) {
                        AVRequest closest = requestMaintainer.getClosest(getRoboTaxiLoc(robotaxi));
                        if (closest != null) {
                            addSharedRoboTaxiPickup(robotaxi, closest);

                            unassignedRoboTaxis.remove(robotaxi);
                            requestMaintainer.remove(closest);
                        }
                    }
                }
            }
            /** Delete the not staying vehicles from the tree as they might move to next link
             * and then they have to be updated in the Quad Tree */

            Collection<RoboTaxi> unassignedRoboTaxisNow = new HashSet<>(unassignedRoboTaxis.getValues());

            for (RoboTaxi robotaxi : unassignedRoboTaxisNow)
                if (!robotaxi.getStatus().equals(RoboTaxiStatus.STAY))
                    unassignedRoboTaxis.remove(robotaxi);

        }

        // ADDITIONAL SHARING POSSIBILITY AT EACH PICKUP
        /** Sharing idea: if a robotaxi Picks up a customer check if other open request
         * are close with similar direction and pick them up. */
        if (round_now % sharingPeriod == 0) {
            Map<AVRequest, RoboTaxi> addedRequests = beamExtensionForSharing.findAssignementAndExecute(getRoboTaxis(), getAVRequests(), this);
            for (Entry<AVRequest, RoboTaxi> entry : addedRequests.entrySet()) {
                // GlobalAssert.that(!unassignedRoboTaxis.contains(entry.getValue()));
                /** a avRequest is not contained in the requestMaintainer if the request was
                 * already assigned before. in that case a removal is not needed */
                if (requestMaintainer.contains(entry.getKey())) {
                    requestMaintainer.remove(entry.getKey());
                }
            }
        }

        /** PARKING EXTENSION */
        parkingStrategy.keepFree(getRoboTaxiSubset(RoboTaxiStatus.STAY), getRoboTaxiSubset(RoboTaxiStatus.REBALANCEDRIVE), round_now)
                .forEach((rt, l) -> setRoboTaxiRebalance(rt, l));
        /** PARKING EXTENSION */
    }

    /** @param request
     * @return {@link Coord} with {@link AVRequest} location */
    /* package */ Tensor getLocation(AVRequest request) {
        Coord coord = request.getFromLink().getFromNode().getCoord();
        return Tensors.vector(coord.getX(), coord.getY());
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ Tensor getRoboTaxiLoc(RoboTaxi roboTaxi) {
        Coord coord = roboTaxi.getDivertableLocation().getCoord();
        return Tensors.vector(coord.getX(), coord.getY());
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

        @Inject(optional = true)
        private ParkingStrategy parkingStrategy;

        @Inject(optional = true)
        private ParkingCapacityAmodeus avSpatialCapacityAmodeus;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            @SuppressWarnings("unused")
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            @SuppressWarnings("unused")
            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            @SuppressWarnings("unused")
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceCost.INSTANCE);

            return new RestrictedLinkCapacityDispatcher(network, config, avconfig, travelTime, router, eventsManager, db, Objects.requireNonNull(parkingStrategy),
                    Objects.requireNonNull(avSpatialCapacityAmodeus));
        }
    }
}
