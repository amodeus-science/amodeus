/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.RequestContainer;
import amodeus.amodeus.net.SimulationObject;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.Increment;

/** count requests */
/* package */ class RequestCountVirtualNodeFunction extends AbstractVirtualNodeFunction {
    public RequestCountVirtualNodeFunction(MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork) {
        super(db, virtualNetwork);
    }

    @Override
    public Tensor evaluate(SimulationObject ref) {
        Tensor count = Array.zeros(virtualNetwork.getvNodesCount());
        for (RequestContainer rc : ref.requests) {
            int linkIndex = rc.fromLinkIndex;
            Link link = db.getOsmLink(linkIndex).link;
            VirtualNode<Link> vn = virtualNetwork.getVirtualNode(link);
            count.set(Increment.ONE, vn.getIndex());
        }
        return count;
    }
}
