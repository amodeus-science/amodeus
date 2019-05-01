/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ interface GlobalBipartiteWeight {

    /** @return the scalar cost between every pair of a {@link RoboTaxi} @param roboTaxi
     *         and a {@link Link} @param link, e.g., the Euclidean distance */
    double between(RoboTaxi roboTaxi, Link link);

}
