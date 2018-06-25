package ch.ethz.idsc.amodeus.dispatcher.shared;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public class SimpleSharedDispatcher extends SharedUniversalDispatcher {

    protected SimpleSharedDispatcher(Config config, AVDispatcherConfig avDispatcherConfig, TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator,
            EventsManager eventsManager) {
        super(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager);
    }

    @Override
    protected void redispatch(double now) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reshuffleSharedAVMenu() {
        // TODO Auto-generated method stub
        
    }

}
