package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/* package */ enum GetAllWithinLess {
    ;
    
    public static List<VirtualNode<Link>> than(Scalar time, GridCell gridCell, //
            VirtualNetwork<Link> virtualNetwork) {
        List<VirtualNode<Link>> closeEnough = new ArrayList<>();
        int i = 1;
        boolean withinLimit = true;
        while (withinLimit && i < virtualNetwork.getvNodesCount()) {
            closeEnough = gridCell.getTimeNClosest(i);
            if (Scalars.lessEquals(time, gridCell.timeTo(closeEnough.get(i - 1)))) {
                withinLimit = false;
            }
            ++i;
        }
        return closeEnough;
    }
}
