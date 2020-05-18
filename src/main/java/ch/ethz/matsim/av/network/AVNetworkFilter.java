package ch.ethz.matsim.av.network;

import org.matsim.api.core.v01.network.Link;

public interface AVNetworkFilter {
    boolean isAllowed(String mode, Link link);
}
