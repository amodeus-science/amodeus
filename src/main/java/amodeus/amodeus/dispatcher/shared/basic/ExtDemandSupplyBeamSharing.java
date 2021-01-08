/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

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

import amodeus.amodeus.dispatcher.DemandSupplyBalancingDispatcher;
import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.core.RebalancingDispatcher;
import amodeus.amodeus.dispatcher.shared.beam.BeamExtensionForSharing;
import amodeus.amodeus.dispatcher.util.TreeMaintainer;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.TensorCoords;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.matsim.SafeConfig;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.opt.Pi;

/** this is a first Shared Dispatcher.
 * 
 * It extends the {@link DemandSupplyBalancingDispatcher}. At each pickup, it is checked if around this Robotaxi there exist other
 * open requests with the same direction. Those are then picked up. */
public class ExtDemandSupplyBeamSharing extends RebalancingDispatcher {
    private final int dispatchPeriod;

    /** ride sharing parameters */
    /** the sharing period says every how many seconds the dispatcher should check if new pickups occurred */
    private final int sharingPeriod; // [s]
    private final BeamExtensionForSharing beamExtensionForSharing;
    /** the maximal angle between the two directions which is allowed that sharing occurs */

    /** data structures are used to enable fast "contains" searching */
    private final TreeMaintainer<PassengerRequest> requestMaintainer;
    private final TreeMaintainer<RoboTaxi> unassignedRoboTaxis;

    protected ExtDemandSupplyBeamSharing(Network network, //
            Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy, RoboTaxiUsageType.SHARED);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(60);
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        sharingPeriod = safeConfig.getInteger("sharingPeriod", 10); // makes sense to choose this value similar to the pickup duration
        double rMax = safeConfig.getDouble("rMax", 1000.0);
        double phiMax = Pi.in(100).multiply(RealScalar.of(safeConfig.getDouble("phiMaxDeg", 5.0) / 180.0)).number().doubleValue();
        beamExtensionForSharing = new BeamExtensionForSharing(rMax, phiMax);
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        this.requestMaintainer = new TreeMaintainer<>(networkBounds, this::getLocation);
        this.unassignedRoboTaxis = new TreeMaintainer<>(networkBounds, this::getRoboTaxiLoc);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            // STANDARD DEMAND SUPPLY IMPLEMENTATION
            /** get open requests and available vehicles */
            Collection<RoboTaxi> robotaxisDivertable = getDivertableUnassignedRoboTaxis();
            robotaxisDivertable.forEach(unassignedRoboTaxis::add);

            Set<PassengerRequest> requests = getUnassignedRequests();
            requests.forEach(requestMaintainer::add);

            if (unassignedRoboTaxis.size() > 0 && requests.size() > 0) {
                /** oversupply case */
                if (unassignedRoboTaxis.size() >= requests.size())
                    for (PassengerRequest avr : requests) {
                        RoboTaxi closest = unassignedRoboTaxis.getClosest(getLocation(avr));
                        if (Objects.nonNull(closest)) {
                            addSharedRoboTaxiPickup(closest, avr, Double.NaN, Double.NaN);
                            unassignedRoboTaxis.remove(closest);
                            requestMaintainer.remove(avr);
                        }
                    }
                /** undersupply case */
                else
                    for (RoboTaxi roboTaxi : robotaxisDivertable) {
                        PassengerRequest closest = requestMaintainer.getClosest(getRoboTaxiLoc(roboTaxi));
                        if (Objects.nonNull(closest)) {
                            addSharedRoboTaxiPickup(roboTaxi, closest, Double.NaN, Double.NaN);
                            unassignedRoboTaxis.remove(roboTaxi);
                            requestMaintainer.remove(closest);
                        }
                    }
            }
            /** Delete the not staying vehicles from the tree as they might move to next link and then they have to be updated in the Quad Tree */

            Collection<RoboTaxi> unassignedRoboTaxisNow = new HashSet<>(unassignedRoboTaxis.getValues());

            for (RoboTaxi roboTaxi : unassignedRoboTaxisNow)
                if (!roboTaxi.getStatus().equals(RoboTaxiStatus.STAY) && unassignedRoboTaxis.contains(roboTaxi))
                    unassignedRoboTaxis.remove(roboTaxi);
        }

        // ADDITIONAL SHARING POSSIBILITY AT EACH PICKUP
        /** Sharing idea: if a robotaxi Picks up a customer check if other open request are close with similar direction and pick them up. */
        if (round_now % sharingPeriod == 0) {
            Map<PassengerRequest, RoboTaxi> addedRequests = beamExtensionForSharing.findAssignementAndExecute(getRoboTaxis(), getPassengerRequests(), this);
            for (Entry<PassengerRequest, RoboTaxi> entry : addedRequests.entrySet()) {
                GlobalAssert.that(!unassignedRoboTaxis.contains(entry.getValue()));
                /** a avRequest is not contained in the requestMaintainer if the request was already assigned before. in that case a removal is not needed */
                if (requestMaintainer.contains(entry.getKey()))
                    requestMaintainer.remove(entry.getKey());
            }
        }
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
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);

            return new ExtDemandSupplyBeamSharing(network, config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy);
        }
    }
}
