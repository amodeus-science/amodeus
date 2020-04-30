package ch.ethz.matsim.av.network;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.data.AVOperator;

public class NullNetworkFilter implements AVNetworkFilter {
	@Override
	public boolean isAllowed(Id<AVOperator> operatorId, Link link) {
		return true;
	}
}
