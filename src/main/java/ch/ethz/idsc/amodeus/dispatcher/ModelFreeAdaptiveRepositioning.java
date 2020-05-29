/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.matsim.amodeus.components.AVDispatcher;
import org.matsim.amodeus.components.AVRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfigWrapper;
import ch.ethz.idsc.amodeus.dispatcher.core.RebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.ConfigurableBipartiteMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceCost;
import ch.ethz.idsc.amodeus.dispatcher.util.FIFOFixedQueue;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatchingILP;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** Implementation of the "+1 method" presented in
 * Ruch, C., Gächter, J., Hakenberg, J. and Frazzoli, E., 2019.
 * The +1 Method: Model-Free Adaptive Repositioning Policies for Robotic Multi-Agent Systems. */
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
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, db);
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
            Map<RoboTaxi, Link> rebalanceMatching = rebalanceMatcher.matchLink(getDivertableRoboTaxis(), rebalanceLinks);
            rebalanceMatching.forEach(this::setRoboTaxiRebalance);

            /** stop vehicles which are still divertable and driving to have only one rebalance vehicles
             * going to a request */
            getDivertableRoboTaxis().stream().filter(rt -> rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE)).forEach(rt -> //
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
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AVRouter router = inject.getModal(AVRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            return new ModelFreeAdaptiveRepositioning( //
                    network, config, operatorConfig, travelTime, router, eventsManager, db);
        }
    }
}