package org.matsim.amodeus.waiting_time;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Network;

public interface WaitingTimeFactory {
    WaitingTime createWaitingTime(AmodeusModeConfig operatorConfig, Network network);
}
