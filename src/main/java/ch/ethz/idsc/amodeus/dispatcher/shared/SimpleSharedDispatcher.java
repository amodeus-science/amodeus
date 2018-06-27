package ch.ethz.idsc.amodeus.dispatcher.shared;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.SharedRoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
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

}
