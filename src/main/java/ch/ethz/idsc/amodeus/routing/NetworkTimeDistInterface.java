/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.tensor.Scalar;

@FunctionalInterface
public interface NetworkTimeDistInterface {

    public Scalar fromTo(Link divertableLocation, Link centerLink);

    public default boolean checkTime(double now) {
        return true;
    }
}
