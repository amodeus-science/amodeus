package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public class TestDispatcherShared extends SharedUniversalDispatcher{

    protected TestDispatcherShared(Config config, AVDispatcherConfig avDispatcherConfig, TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator,
            EventsManager eventsManager, MatsimAmodeusDatabase db) {
        super(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager, db);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void redispatch(double now) {

    }

    public void setUp(ArtificialScenarioCreator s) {
        addVehicle(s.vehicle1);
        addVehicle(s.vehicle2);
        onRequestSubmitted(s.avRequest1);
        onRequestSubmitted(s.avRequest2);
        onRequestSubmitted(s.avRequest3);
        onRequestSubmitted(s.avRequest4);
        onRequestSubmitted(s.avRequest5);
        onRequestSubmitted(s.avRequest6);
        onRequestSubmitted(s.avRequest7);
        
    }
}
