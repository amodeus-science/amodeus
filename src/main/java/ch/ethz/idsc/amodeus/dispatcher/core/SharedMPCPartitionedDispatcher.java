package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.mpcsetup.MPCsetup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public abstract class SharedMPCPartitionedDispatcher extends SharedPartitionedDispatcher {
    protected final MPCsetup mpcSetup; //

    protected SharedMPCPartitionedDispatcher( //
            Config config, //
            AVDispatcherConfig avconfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            VirtualNetwork<Link> virtualNetwork, //
            MPCsetup mpcSetup, //
            MatsimAmodeusDatabase db) {
        super(config, avconfig, travelTime, router, eventsManager, virtualNetwork, db);

        if (mpcSetup == null) {
            throw new IllegalStateException(
                    "The MPC controller is not set.");
        }

        this.mpcSetup = Objects.requireNonNull(mpcSetup);
        
    }

    
}
