package ch.ethz.matsim.av.waiting_time;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.matsim.av.config.AmodeusModeConfig;

public interface WaitingTimeFactory {
    WaitingTime createWaitingTime(AmodeusModeConfig operatorConfig, Network network);
}
