/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.tensor.Scalar;

// TODO @clruch can we put this together with the {@link DistanceFunction} interface? No need for redundancy...
public interface NetworkTimeDistInterface {

    /** @param from
     * @param to
     * @param now
     * @return */
    Scalar travelTime(Link from, Link to, double now);

    /** @param from
     * @param to
     * @param now
     * @return */
    Scalar distance(Link from, Link to, double now);

}
