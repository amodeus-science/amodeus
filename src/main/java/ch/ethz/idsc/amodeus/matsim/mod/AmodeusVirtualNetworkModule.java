/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataGet;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkGet;
import ch.ethz.matsim.av.data.AVOperator;

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusVirtualNetworkModule extends AbstractModule {
    private final ScenarioOptions scenarioOptions;

    public AmodeusVirtualNetworkModule(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public Map<Id<AVOperator>, VirtualNetwork<Link>> provideVirtualNetworks(Map<Id<AVOperator>, Network> networks) {
        try {
            Map<Id<AVOperator>, VirtualNetwork<Link>> virtualNetworks = new HashMap<>();

            for (Map.Entry<Id<AVOperator>, Network> entry : networks.entrySet()) {
                virtualNetworks.put(entry.getKey(), VirtualNetworkGet.readDefault(entry.getValue(), scenarioOptions));
            }

            return virtualNetworks;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    public Map<Id<AVOperator>, TravelData> provideTravelDatas(Map<Id<AVOperator>, VirtualNetwork<Link>> virtualNetworks) {
        Map<Id<AVOperator>, TravelData> travelDatas = new HashMap<>();

        for (Map.Entry<Id<AVOperator>, VirtualNetwork<Link>> entry : virtualNetworks.entrySet()) {
            travelDatas.put(entry.getKey(), TravelDataGet.readStatic(entry.getValue(), scenarioOptions));
        }

        return travelDatas;
    }
}
