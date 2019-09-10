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
        for (VehicleContainer vehicleContainer : ref.vehicles) {
            int linkIndex = vehicleContainer.linkIndex;
            Link link = db.getOsmLink(linkIndex).link;
            VirtualNode<Link> virtualNode = virtualNetwork.getVirtualNode(link);
            count.set(Increment.ONE, virtualNode.getIndex());
        }
        return count;
    }
}
