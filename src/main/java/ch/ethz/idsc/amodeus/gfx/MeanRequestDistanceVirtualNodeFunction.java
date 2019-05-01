/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** mean request distance */
/* package */ class MeanRequestDistanceVirtualNodeFunction extends AbstractVirtualNodeFunction {

    public MeanRequestDistanceVirtualNodeFunction(MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork) {
        super(db, virtualNetwork);
    }

    @Override
    public Tensor evaluate(SimulationObject ref) {
        Tensor collect = Tensors.vector(i -> Tensors.empty(), virtualNetwork.getvNodesCount());
        for (RequestContainer rc : ref.requests) {
            Link linkAnte = db.getOsmLink(rc.fromLinkIndex).link;
            Link linkPost = db.getOsmLink(rc.toLinkIndex).link;
            double distance = Math.hypot( //
                    linkAnte.getCoord().getX() - linkPost.getCoord().getX(), //
                    linkAnte.getCoord().getY() - linkPost.getCoord().getY());
            VirtualNode<Link> vn = virtualNetwork.getVirtualNode(linkAnte);
            collect.set(s -> s.append(DoubleScalar.of(distance)), vn.getIndex());
        }

        return Tensors.vector( //
                i -> StaticHelper.meanOrZero(collect.get(i)), //
                virtualNetwork.getvNodesCount());
    }

}
