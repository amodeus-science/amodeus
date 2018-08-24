/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.io.IOException;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataGet;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkGet;

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusVirtualNetworkModule extends AbstractModule {
    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public VirtualNetwork<Link> provideVirtualNetwork(Network network) {
        try {
            return VirtualNetworkGet.readDefault(network);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    public TravelData provideTravelData(VirtualNetwork<Link> virtualNetwork) {
        try {
            return TravelDataGet.readDefault(virtualNetwork);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
