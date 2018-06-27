package ch.ethz.idsc.amodeus.dispatcher.shared;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.SharedRoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.HungarBiPartVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public class SimpleSharedDispatcher extends SharedUniversalDispatcher {

	private final int dispatchPeriod;

	protected SimpleSharedDispatcher(Config config, AVDispatcherConfig avDispatcherConfig, TravelTime travelTime,
			ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, EventsManager eventsManager) {
		super(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager);
		SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
		dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);

	}

	@Override
	protected void redispatch(double now) {
		final long round_now = Math.round(now);

		if (round_now % dispatchPeriod == 0) {
			for (SharedRoboTaxi sharedRoboTaxi : getDivertableUnassignedRoboTaxis()) {
				if (getUnassignedAVRequests().size() >= 2) {

					AVRequest firstRequest = getUnassignedAVRequests().get(0);
					AVRequest secondRequest = getUnassignedAVRequests().get(1);
					addSharedRoboTaxiPickup(sharedRoboTaxi, firstRequest);
					addSharedRoboTaxiPickup(sharedRoboTaxi, secondRequest);
					// TODO CHECK the menu manipulation
					SharedAVCourse sharedAVCourse = new SharedAVCourse(secondRequest.getId(), SharedAVMealType.PICKUP);
					sharedRoboTaxi.getMenu().moveAVCourseToPrev(sharedAVCourse);
				} else {
					// TODO Improve and make function without break
					break;
				}
			}
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

	        @Inject(optional = true)
	        private TravelData travelData;

	        @Inject
	        private Config config;

	        @Override
	        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig) {
	            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

	            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
	            AbstractVehicleDestMatcher abstractVehicleDestMatcher = new HungarBiPartVehicleDestMatcher(new EuclideanDistanceFunction());

	            return new SimpleSharedDispatcher(config, avconfig, travelTime,
	            		router, eventsManager);
	        }
	    }

}
