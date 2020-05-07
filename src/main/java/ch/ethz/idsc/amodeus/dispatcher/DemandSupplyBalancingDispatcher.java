/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfigWrapper;
import ch.ethz.idsc.amodeus.dispatcher.core.RebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMaintainer;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** Implementation of the "demand-supply-balancing" dispatching heuristic presented in
 * Maciejewski, Michal, and Joschka Bischoff. "Large-scale microscopic simulation of taxi services."
 * Procedia Computer Science 52 (2015): 358-364.
 * 
 * This dispatcher is not a dispatcher with rebalancing functionality, it could also be derived from
 * the UniversalDispatcher, but in order to allow extended versions to use the setRoboTaxiRebalance
 * functionality, it was extended from the abstract RebalancingDispatcher. */
public class DemandSupplyBalancingDispatcher extends RebalancingDispatcher {

    private final int dispatchPeriod;
    /** data structures are used to enable fast "contains" searching */
    private final TreeMaintainer<AVRequest> requestMaintainer;
    private final TreeMaintainer<RoboTaxi> unassignedRoboTaxis;

    protected DemandSupplyBalancingDispatcher(Config config, OperatorConfig operatorConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, Network network, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, db);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(10);
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        this.requestMaintainer = new TreeMaintainer<>(networkBounds, this::getLocation);
        this.unassignedRoboTaxis = new TreeMaintainer<>(networkBounds, this::getRoboTaxiLoc);
    }

    @Override
    public void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            /** get open requests and available vehicles */
            Collection<RoboTaxi> roboTaxisDivertable = getDivertableUnassignedRoboTaxis();
            getRoboTaxiSubset(RoboTaxiStatus.STAY).forEach(unassignedRoboTaxis::add);
            List<AVRequest> requests = getUnassignedAVRequests();
            requests.forEach(requestMaintainer::add);

            /** distinguish over- and undersupply cases */
            boolean oversupply = false;
            if (unassignedRoboTaxis.size() >= requests.size())
                oversupply = true;

            if (unassignedRoboTaxis.size() > 0 && requests.size() > 0)
                /** oversupply case */
                if (oversupply)
                    for (AVRequest avr : requests) {
                        RoboTaxi closest = unassignedRoboTaxis.getClosest(getLocation(avr));
                        if (closest != null) {
                            setRoboTaxiPickup(closest, avr);
                            unassignedRoboTaxis.remove(closest);
                            requestMaintainer.remove(avr);
                        }
                    }
                /** undersupply case */
                else
                    for (RoboTaxi roboTaxi : roboTaxisDivertable) {
                        Coord coord = roboTaxi.getDivertableLocation().getFromNode().getCoord();
                        Tensor tCoord = Tensors.vector(coord.getX(), coord.getY());
                        AVRequest closest = requestMaintainer.getClosest(tCoord);
                        if (Objects.nonNull(closest)) {
                            setRoboTaxiPickup(roboTaxi, closest);
                            unassignedRoboTaxis.remove(roboTaxi);
                            requestMaintainer.remove(closest);
                        }
                    }
        }
    }

    /** @param request
     * @return {@link Coord} with {@link AVRequest} location */
    /* package */ Tensor getLocation(AVRequest request) {
        return TensorCoords.toTensor(request.getFromLink().getFromNode().getCoord());
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ Tensor getRoboTaxiLoc(RoboTaxi roboTaxi) {
        return TensorCoords.toTensor(roboTaxi.getDivertableLocation().getCoord());
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            OperatorConfig operatorConfig = inject.getModal(OperatorConfig.class);
            Network network = inject.getModal(Network.class);
            AVRouter router = inject.getModal(AVRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            return new DemandSupplyBalancingDispatcher( //
                    config, operatorConfig, travelTime, //
                    router, eventsManager, network, db);
        }
    }
}