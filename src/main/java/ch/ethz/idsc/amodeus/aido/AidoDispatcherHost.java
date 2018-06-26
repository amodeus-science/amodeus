/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public class AidoDispatcherHost extends RebalancingDispatcher {

    private Map<Integer, RoboTaxi> idRoboTaxiMap = new HashMap(); // TODO fill the map
    private Map<Integer, AVRequest> idRequestMap = new HashMap(); // TODO fill the map
    private final FastLinkLookup fastLinkLookup;
    private final StringClientSocket clientSocket;

    protected AidoDispatcherHost(Network network, Config config, AVDispatcherConfig avDispatcherConfig, TravelTime travelTime,
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, EventsManager eventsManager, //
            StringClientSocket clientSocket) {
        super(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager);
        this.clientSocket = clientSocket;
        this.fastLinkLookup = new FastLinkLookup(network, MatsimStaticDatabase.INSTANCE);

    }

    @Override
    protected void redispatch(double now) {
        try {
            Tensor status = Tensors.of(RealScalar.of((long) now), //
                    AidoRoboTaxiCompiler.compile(getRoboTaxis()), //
                    AidoRequestCompiler.compile(getAVRequests()));
            clientSocket.write(status.toString() + '\n');

            String fromClient = null;

            fromClient = clientSocket.reader.readLine();

            Tensor commands = Tensors.fromString(fromClient);
            // TODO consistency checks
            Tensor pickups = commands.get(0);
            for (Tensor pickup : pickups) {
                RoboTaxi roboTaxi = idRoboTaxiMap.get(pickup.Get(0).number().intValue());
                AVRequest avRequest = idRequestMap.get(pickup.Get(1).number().intValue());
                setRoboTaxiPickup(roboTaxi, avRequest);
            }

            Tensor rebalances = commands.get(1);
            for (Tensor rebalance : rebalances) {
                RoboTaxi roboTaxi = idRoboTaxiMap.get(rebalance.Get(0).number().intValue());
                Link link = fastLinkLookup.getLinkCHANGENAME(TensorCoords.toCoord(rebalance.get(1)));
                setRoboTaxiRebalance(roboTaxi, link);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

        @Inject
        @Named(AVModule.AV_MODE)
        private Network network;

        @Inject
        private Config config;

        private StringClientSocket serverSocket = null; // TODO

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig) {
            return new AidoDispatcherHost( //
                    network, config, avconfig, travelTime, router, eventsManager, serverSocket);
        }
    }

}
