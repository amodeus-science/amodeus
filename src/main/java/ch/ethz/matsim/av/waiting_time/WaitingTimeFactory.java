package ch.ethz.matsim.av.waiting_time;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.matsim.av.config.operator.OperatorConfig;

public interface WaitingTimeFactory {
	WaitingTime createWaitingTime(OperatorConfig operatorConfig, Network network);
}
