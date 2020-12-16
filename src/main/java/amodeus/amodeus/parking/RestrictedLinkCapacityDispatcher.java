/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;
import amodeus.amodeus.dispatcher.core.SharedRebalancingDispatcher;
import amodeus.amodeus.dispatcher.shared.basic.ExtDemandSupplyBeamSharing;
import amodeus.amodeus.dispatcher.shared.beam.BeamExtensionForSharing;
import amodeus.amodeus.dispatcher.util.DistanceHeuristics;
import amodeus.amodeus.dispatcher.util.TreeMaintainer;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.TensorCoords;
import amodeus.amodeus.parking.capacities.ParkingCapacity;
import amodeus.amodeus.parking.strategies.ParkingStrategy;
import amodeus.amodeus.util.matsim.SafeConfig;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.opt.Pi;

/** This dispatcher takes the Parking Situation into Account.
 * 
 * To run this dipatcher it is required that
 * 1. The MATSim controler (e.g. in the ScenarioServer) uses the {@link AmodeusParkingModule}.
 * 2. Set the values for the {@link ParkingCapacity} and the {@link ParkingStrategy} in the
 * AMoDeusOptions.properties file
 * 3. Choose the {@link RestrictedLinkCapacityDispatcher} in the av.xml configuration
 * 
 * It extends the {@link ExtDemandSupplyBeamSharing}. At each pickup it is
 * checked if around this {@link RoboTaxi} there exist other Open requests with the same
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
    private final TreeMaintainer<PassengerRequest> requestMaintainer;
    private final double[] networkBounds;

    protected RestrictedLinkCapacityDispatcher(Network network, //
            Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, ParkingStrategy parkingStrategy, //
            ParkingCapacity avSpatialCapacityAmodeus, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(60);
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        sharingPeriod = safeConfig.getInteger("sharingPeriod", 10); // makes sense to choose this value similar to the pickup duration
        double rMax = safeConfig.getDouble("rMax", 1000.0);
        double phiMax = Pi.in(100).multiply(RealScalar.of(safeConfig.getDouble("phiMaxDeg", 5.0) / 180.0)).number().doubleValue();
        beamExtensionForSharing = new BeamExtensionForSharing(rMax, phiMax);
        this.networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        this.requestMaintainer = new TreeMaintainer<>(networkBounds, this::getLocation);

        /** PARKING EXTENSION */
        this.parkingStrategy = parkingStrategy;
        DistanceHeuristics distanceHeuristics = //
                dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.ASTARLANDMARKS);
        this.parkingStrategy.setRuntimeParameters(avSpatialCapacityAmodeus, network, distanceHeuristics.getDistanceFunction(network));
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

            robotaxisDivertable.forEach(unassignedRoboTaxis::add);

            Set<PassengerRequest> requests = getUnassignedRequests();
            requests.forEach(requestMaintainer::add);

            /** distinguish over- and undersupply cases */
            boolean oversupply = unassignedRoboTaxis.size() >= requests.size();

            if (unassignedRoboTaxis.size() > 0 && requests.size() > 0) {
                /** oversupply case */
                if (oversupply)
                    for (PassengerRequest avr : requests) {
                        RoboTaxi closest = unassignedRoboTaxis.getClosest(getLocation(avr));
                        if (Objects.nonNull(closest)) {
                            addSharedRoboTaxiPickup(closest, avr);

                            unassignedRoboTaxis.remove(closest);
                            requestMaintainer.remove(avr);
                        }
                    }
                /** undersupply case */
                else
                    for (RoboTaxi roboTaxi : robotaxisDivertable) {
                        PassengerRequest closest = requestMaintainer.getClosest(getRoboTaxiLoc(roboTaxi));
                        if (Objects.nonNull(closest)) {
                            addSharedRoboTaxiPickup(roboTaxi, closest);

                            unassignedRoboTaxis.remove(roboTaxi);
                            requestMaintainer.remove(closest);
                        }
                    }
            }
            /** Delete the not staying vehicles from the tree as they might move to next link
             * and then they have to be updated in the Quad Tree */
            unassignedRoboTaxis.getValues().stream().filter(rt -> !rt.getStatus().equals(RoboTaxiStatus.STAY)).collect(Collectors.toSet()).forEach(unassignedRoboTaxis::remove);
        }

        // ADDITIONAL SHARING POSSIBILITY AT EACH PICKUP
        /** Sharing idea: if a robotaxi Picks up a customer check if other open request
         * are close with similar direction and pick them up. */
        if (round_now % sharingPeriod == 0) {
            Map<PassengerRequest, RoboTaxi> addedRequests = beamExtensionForSharing.findAssignementAndExecute(getRoboTaxis(), getPassengerRequests(), this);
            for (PassengerRequest avRequest : addedRequests.keySet())
                /** a avRequest is not contained in the requestMaintainer if the request was
                 * already assigned before. in that case a removal is not needed */
                if (requestMaintainer.contains(avRequest))
                    requestMaintainer.remove(avRequest);
        }

        /** PARKING EXTENSION */
        parkingStrategy.keepFree(getRoboTaxiSubset(RoboTaxiStatus.STAY), getRoboTaxiSubset(RoboTaxiStatus.REBALANCEDRIVE), round_now).forEach(this::setRoboTaxiRebalance);
        /** PARKING EXTENSION */
    }

    /** @param request
     * @return {@link Coord} with {@link PassengerRequest} location */
    /* package */ Tensor getLocation(PassengerRequest request) {
        return TensorCoords.toTensor(request.getFromLink().getFromNode().getCoord());
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ Tensor getRoboTaxiLoc(RoboTaxi roboTaxi) {
        return TensorCoords.toTensor(roboTaxi.getDivertableLocation().getCoord());
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            EventsManager eventsManager = inject.get(EventsManager.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);

            Network network = inject.getModal(Network.class);
            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);

            // TODO: Eventually, if parking should be configurable per mode, this should be made modal.
            ParkingStrategy parkingStrategy = inject.get(ParkingStrategy.class);
            ParkingCapacity avSpatialCapacityAmodeus = inject.get(ParkingCapacity.class);

            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);

            return new RestrictedLinkCapacityDispatcher(network, config, operatorConfig, travelTime, router, eventsManager, db, //
                    Objects.requireNonNull(parkingStrategy), Objects.requireNonNull(avSpatialCapacityAmodeus), rebalancingStrategy);
        }
    }
}
