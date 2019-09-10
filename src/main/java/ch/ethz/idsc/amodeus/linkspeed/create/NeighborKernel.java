/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.Collection;

import org.matsim.api.core.v01.network.Link;

/** A {@link NeighborKernel} is used to identify the neighboring {@link Link}s
 * of a given {@link Link} in order to approximate traffic flow information
 * at this link. */
public interface NeighborKernel {

    public Collection<Link> getNeighbors(Link link);

}
