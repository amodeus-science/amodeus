/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.Increment;

/** count vehicles */
/* package */ class VehicleCountVirtualNodeFunction extends AbstractVirtualNodeFunction {
    public VehicleCountVirtualNodeFunction(MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork) {
        super(db, virtualNetwork);
    }

    @Override
    public Tensor evaluate(SimulationObject ref) {
        Tensor count = Array.zeros(virtualNetwork.getvNodesCount());
        for (VehicleContainer vc : ref.vehicles) {
<<<<<<< HEAD
            int linkIndex = vc.getLastLinkIndex();
=======
            int linkIndex = vc.linkTrace[vc.linkTrace.length - 1];
>>>>>>> master
            Link link = db.getOsmLink(linkIndex).link;
            VirtualNode<Link> vn = virtualNetwork.getVirtualNode(link);
            count.set(Increment.ONE, vn.getIndex());
        }
        return count;
    }
}
