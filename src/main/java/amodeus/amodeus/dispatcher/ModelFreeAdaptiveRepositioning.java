/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusDispatcher.AVDispatcherFactory;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.modal.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.core.RebalancingDispatcher;
import amodeus.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import amodeus.amodeus.dispatcher.util.BipartiteMatcher;
import amodeus.amodeus.dispatcher.util.ConfigurableBipartiteMatcher;
import amodeus.amodeus.dispatcher.util.EuclideanDistanceCost;
import amodeus.amodeus.dispatcher.util.FIFOFixedQueue;
import amodeus.amodeus.dispatcher.util.GlobalBipartiteMatching;
import amodeus.amodeus.dispatcher.util.GlobalBipartiteMatchingILP;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.routing.EuclideanDistanceFunction;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.matsim.SafeConfig;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/**
 * Implementation of the "+1 method" presented in
 * Ruch, C., Gächter, J., Hakenberg, J. and Frazzoli, E., 2019.
 * The +1 Method: Model-Free Adaptive Repositioning Policies for Robotic
 * Multi-Agent Systems.
 */
public class ModelFreeAdaptiveRepositioning extends RebalancingDispatcher {
    private final Network network;
    private final BipartiteMatcher assignmentMatcher;
    private final AbstractRoboTaxiDestMatcher rebalanceMatcher;

    private Tensor printVals = Tensors.empty();

    private final int dispatchPeriod;
    private final int rebalancingPeriod;

    /** list of last known request locations */
    private final FIFOFixedQueue<Link> lastRebLoc;
    private HashSet<PassengerRequest> registeredRequests = new HashSet<>();

    private ModelFreeAdaptiveRepositioning(Network network, Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy,
                RoboTaxiUsageType.SINGLEUSED);
        this.network = network;
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        rebalancingPeriod = dispatcherConfig.getRebalancingPeriod(900);
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig);
        assignmentMatcher = new ConfigurableBipartiteMatcher(network, EuclideanDistanceCost.INSTANCE, safeConfig);
        String rebWeight = safeConfig.getString("matchingReb", "HUNGARIAN");
        if (rebWeight.equals("HUNGARIAN")) {
            rebalanceMatcher = new GlobalBipartiteMatching(EuclideanDistanceCost.INSTANCE);
        } else {
            Tensor weights = Tensors.fromString(rebWeight);
            rebalanceMatcher = new GlobalBipartiteMatchingILP(EuclideanDistanceCost.INSTANCE, weights);
        }
        long numRT = operatorConfig.getGeneratorConfig().getNumberOfVehicles();
        lastRebLoc = new FIFOFixedQueue<>((int) numRT);

        System.out.println("dispatchPeriod:  " + dispatchPeriod);
        System.out.println("rebalancePeriod: " + rebalancingPeriod);
    }

    @Override
    public void redispatch(double now) {
        final long round_now = Math.round(now);

        /** take account of newly arrived requests */
        getPassengerRequests().stream().filter(avr -> !registeredRequests.contains(avr)).forEach(avr -> {
            lastRebLoc.manage(avr.getFromLink());
            registeredRequests.add(avr);
        });

        /** dipatch step */
        if (round_now % dispatchPeriod == 0)
            /** step 1, execute pickup on all open requests */
            printVals = assignmentMatcher.executePickup(this, getDivertableRoboTaxis(), getPassengerRequests(), //
                    EuclideanDistanceFunction.INSTANCE, network);

        /** rebalancing step */
        if (round_now % rebalancingPeriod == 0) {
            /** step 2, perform rebalancing on last known request locations */
            Collection<Link> rebalanceLinks = getLastRebalanceLocations(getDivertableRoboTaxis().size());
            Map<RoboTaxi, Link> rebalanceMatching = rebalanceMatcher.matchLink(getDivertableRoboTaxis(),
                    rebalanceLinks);
            rebalanceMatching.forEach(this::setRoboTaxiRebalance);

            /**
             * stop vehicles which are still divertable and driving to have only one
             * rebalance vehicles
             * going to a request
             */
            getDivertableRoboTaxis().stream().filter(rt -> rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE))
                    .forEach(rt -> //
                    setRoboTaxiRebalance(rt, rt.getDivertableLocation()));
        }
    }

    private final Collection<Link> getLastRebalanceLocations(int rebLocNum) {
        GlobalAssert.that(Objects.nonNull(lastRebLoc));
        return lastRebLoc.getNewest(rebLocNum);
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s H=%s", //
                super.getInfoLine(), //
                printVals.toString() //
        );
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = (Config) inject.get(Config.class);
            MatsimAmodeusDatabase db = (MatsimAmodeusDatabase) inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = (EventsManager) inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = (AmodeusModeConfig) inject.getModal(AmodeusModeConfig.class);
            Network network = (Network) inject.getModal(Network.class);
            AmodeusRouter router = (AmodeusRouter) inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = (TravelTime) inject.getModal(TravelTime.class);
            RebalancingStrategy rebalancingStrategy = (RebalancingStrategy) inject.getModal(RebalancingStrategy.class);

            return new ModelFreeAdaptiveRepositioning( //
                    network, config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy);
        }
    }
}