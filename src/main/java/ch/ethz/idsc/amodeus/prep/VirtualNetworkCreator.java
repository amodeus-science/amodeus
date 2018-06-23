package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public interface VirtualNetworkCreator {
    VirtualNetwork<Link> create(Network network, Population population);
}
