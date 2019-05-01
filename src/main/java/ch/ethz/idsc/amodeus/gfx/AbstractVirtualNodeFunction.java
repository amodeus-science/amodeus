/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

/* package */ abstract class AbstractVirtualNodeFunction implements VirtualNodeFunction {
    final MatsimAmodeusDatabase db;
    final VirtualNetwork<Link> virtualNetwork;

    public AbstractVirtualNodeFunction(MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork) {
        this.db = db;
        this.virtualNetwork = virtualNetwork;
    }
}
