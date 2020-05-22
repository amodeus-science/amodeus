package ch.ethz.matsim.av.network;

import org.matsim.api.core.v01.network.Link;

public class NullNetworkFilter implements AVNetworkFilter {
    @Override
    public boolean isAllowed(String mode, Link link) {
        return true;
    }
}
