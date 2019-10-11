/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class AmodeusModule extends AbstractModule {
    @Override
    public void install() {
        /* This has been added after upgrading Amodeus' dependency to MATSim:0.10.1. In the mean time, DVRP got a new "initial travel time" which resembles
         * better what
         * QSim produces (since the time-step (seconds) based QSim produces travel times that are "rounded" to the next integer). This is now considered in DVRP
         * 0.10.1,
         * but it was not when Amodeus' unit tests had been written. Hence, to leave the unit tests intact, we add here the old FreeSpeedTravelTime. For
         * reference, the
         * MATSim commit, that introduced the new TravelTime implementation in DVRP is be380cfc72e1c29d840fcc6b71a4bebeb3e567c1 . */
        addTravelTimeBinding(DvrpTravelTimeModule.DVRP_INITIAL).toInstance(new FreeSpeedTravelTime());

        installQSimModule(new AmodeusQSimModule());
    }

    @Provides
    @Singleton
    @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING)
    public Network provideAVNetwork(Network fullNetwork) {
        /* TODO MISC Here we provide the FULL network with public transit links etc.,
         * because this is how Amodeus has been set up initially. This was not a problem,
         * since the av package also provides this network by default. However,
         * this will change so in order to keep backward compatibility, we explicitly provide the
         * full network here. Eventually Amodeus should be able to cope with what is defined by default. */

        return fullNetwork;
    }

}
