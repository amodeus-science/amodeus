package ch.ethz.matsim.av.network;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.data.AVOperator;

public interface AVNetworkFilter {
    boolean isAllowed(Id<AVOperator> operatorId, Link link);
}
