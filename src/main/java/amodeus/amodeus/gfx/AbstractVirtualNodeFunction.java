/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;

/* package */ abstract class AbstractVirtualNodeFunction implements VirtualNodeFunction {
    final MatsimAmodeusDatabase db;
    final VirtualNetwork<Link> virtualNetwork;

    public AbstractVirtualNodeFunction(MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork) {
        this.db = db;
        this.virtualNetwork = virtualNetwork;
    }
}
