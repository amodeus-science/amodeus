/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.tensor.Scalar;

public interface TravelTimeCalculator {

    Scalar timeFromTo(Link divertableLocation, Link centerLink);

    default boolean isForNow(double now) {
        return true;
    }
}
