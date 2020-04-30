package ch.ethz.matsim.av.waiting_time;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.WaitingTimeConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class WaitingTimeModule extends AbstractModule {
	@Override
	public void install() {
		if (getDynamicOperators(AVConfigGroup.getOrCreate(getConfig())).size() > 0) {
			bind(WaitingTimeListener.class);
			addEventHandlerBinding().to(WaitingTimeListener.class);
			addControlerListenerBinding().to(WaitingTimeListener.class);
		}

		bind(StandardWaitingTimeFactory.class);
		bind(WaitingTimeFactory.class).to(StandardWaitingTimeFactory.class);
	}

	static public Collection<Id<AVOperator>> getDynamicOperators(AVConfigGroup config) {
		Set<Id<AVOperator>> dynamicIds = new HashSet<>();

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			WaitingTimeConfig waitingConfig = operatorConfig.getWaitingTimeConfig();

			if (waitingConfig.getEstimationAlpha() > 0.0) {
				dynamicIds.add(operatorConfig.getId());
			}
		}

		return dynamicIds;
	}

	@Singleton
	@Provides
	public Map<Id<AVOperator>, WaitingTime> provideWaitingTimes(AVConfigGroup config, WaitingTimeFactory factory,
			Map<Id<AVOperator>, Network> networks) {
		Map<Id<AVOperator>, WaitingTime> waitingTimes = new HashMap<>();

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			Network network = networks.get(operatorConfig.getId());
			WaitingTime waitingTime = factory.createWaitingTime(operatorConfig, network);
			waitingTimes.put(operatorConfig.getId(), waitingTime);
		}

		return waitingTimes;
	}

	@Singleton
	@Provides
	public Map<Id<AVOperator>, WaitingTimeCollector> provideCollectors(AVConfigGroup config,
			Map<Id<AVOperator>, WaitingTime> waitingTimes) {
		Map<Id<AVOperator>, WaitingTimeCollector> collectors = new HashMap<>();
		Collection<Id<AVOperator>> dynamicOperatorIds = getDynamicOperators(config);

		for (Map.Entry<Id<AVOperator>, WaitingTime> entry : waitingTimes.entrySet()) {
			if (dynamicOperatorIds.contains(entry.getKey())) {
				WaitingTime waitingTime = entry.getValue();

				if (waitingTime instanceof WaitingTimeCollector) {
					collectors.put(entry.getKey(), (WaitingTimeCollector) waitingTime);
				}
			}
		}

		return collectors;
	}
}
