/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.Increment;

/** count requests */
/* package */ class RequestCountVirtualNodeFunction extends AbstractVirtualNodeFunction {
    public RequestCountVirtualNodeFunction(MatsimStaticDatabase db, VirtualNetwork<Link> virtualNetwork) {
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
