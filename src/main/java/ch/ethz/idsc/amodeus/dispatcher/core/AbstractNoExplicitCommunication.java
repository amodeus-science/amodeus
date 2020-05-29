/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.amodeus.components.AVRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.cycling.CyclePreventer;
import ch.ethz.idsc.amodeus.dispatcher.util.DrivebyRequestStopper;
import ch.ethz.idsc.amodeus.dispatcher.util.TensorLocation;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMaintainer;
import ch.ethz.idsc.amodeus.dispatcher.util.WeberMaintainer;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

/** Arsie, Alessandro, Ketan Savla, and Emilio Frazzoli. "Efficient routing algorithms for multiple
 * vehicles with no explicit communications." IEEE Transactions on Automatic Control 54.10 (2009): 2302-2317. */
public abstract class AbstractNoExplicitCommunication extends RebalancingDispatcher {
    private final int dispatchPeriod;
    private final Network network;
    private final CyclePreventer cyclePreventer = new CyclePreventer();
    protected final Map<RoboTaxi, WeberMaintainer> weberMaintainers = new HashMap<>();
    protected final TreeMaintainer<PassengerRequest> requestMaintainer;

    protected AbstractNoExplicitCommunication(Network network, Config config, //
            AmodeusModeConfig operatorConfig, TravelTime travelTime, //
            AVRouter router, EventsManager eventsManager, MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, db);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        this.network = network;
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        requestMaintainer = new TreeMaintainer<>(networkBounds, TensorLocation::of);
    }

    @Override
    protected void redispatch(double now) {
        /** ensure that {@link RoboTaxi}s are not driving cycles (due to Euclidean distance measure in algorithm) */
        cyclePreventer.update(getRoboTaxis(), this::setRoboTaxiRebalance);

        /** get set D(t), the open requests */
        Collection<PassengerRequest> d = getPassengerRequests();
        d.forEach(requestMaintainer::add);

        /** as soon as {@link RoboTaxi}s appear, initialize with present location */
        if (weberMaintainers.isEmpty())
            getRoboTaxis().forEach(rt -> weberMaintainers.put(rt, new WeberMaintainer(rt.getDivertableLocation(), network)));

        /** if a {@link RoboTaxi} is on the same {@link Link} as a {@link AVRquest}, a pickup
         * is executed */
        Map<RoboTaxi, PassengerRequest> matched = DrivebyRequestStopper.stopDrivingBy(DispatcherUtils.getPassengerRequestsAtLinks(getPassengerRequests()), //
                getDivertableRoboTaxis(), this::setRoboTaxiPickup);
        matched.values().forEach(requestMaintainer::remove);

        /** add all successful pickups to the {@link WeberMaintainer}s */
        matched.forEach((rt, avr) -> weberMaintainers.get(rt).update(avr));

        final long round_now = Math.round(now);
        if (round_now % dispatchPeriod == 0) {
            cyclePreventer.printStatus();

            redispatchIteration();
        }

        /** Save Weber maintainers */
        if (round_now == 107500) // TODO @clruch check hardcoded
            try {
                WeberMaintainer.saveWeberLocations(weberMaintainers, MultiFileTools.getDefaultWorkingDirectory());
            } catch (Exception ex) {
                System.err.println("not able to save weber maintainers");
                ex.printStackTrace();
            }
    }

    protected abstract void redispatchIteration();
}
