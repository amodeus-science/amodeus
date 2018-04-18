package ch.ethz.idsc.amodeus.traveldata.iterative;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.matsim.av.framework.AVModule;

public class IterativeTravelDataModule extends AbstractModule {
    @Override
    public void install() {
        addControlerListenerBinding().to(TravelDataListener.class);
        addEventHandlerBinding().to(TravelDataListener.class);
    }

    @Provides
    @Singleton
    public TravelDataListener provideTravelDataListener(VirtualNetwork<Link> virtualNetwork, @Named(AVModule.AV_MODE) Network network, TravelData travelData) {
        return new TravelDataListener(virtualNetwork, network, travelData);
    }
}
