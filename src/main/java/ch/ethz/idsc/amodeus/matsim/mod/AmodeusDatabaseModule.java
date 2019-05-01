/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusDatabaseModule extends AbstractModule {
    private final MatsimAmodeusDatabase db;

    public AmodeusDatabaseModule(MatsimAmodeusDatabase db) {
        this.db = db;
    }

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public MatsimAmodeusDatabase provideDatabase() {
        return db;
    }

}
