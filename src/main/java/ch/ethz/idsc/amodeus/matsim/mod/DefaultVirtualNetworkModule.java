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

public class DefaultVirtualNetworkModule extends AbstractModule {
    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public VirtualNetwork<Link> provideVirtualNetwork(Network network) {
        // Here we provide the standard VirtualNetwork from the working directory

        try {
            return VirtualNetworkGet.readDefault(network);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    public TravelData provideTravelData(VirtualNetwork<Link> virtualNetwork) {
        // Here we provide the standard VirtualNetwork from the working directory

        try {
            return TravelDataGet.readDefault(virtualNetwork);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
