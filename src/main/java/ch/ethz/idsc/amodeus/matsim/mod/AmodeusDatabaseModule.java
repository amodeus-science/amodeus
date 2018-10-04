package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

/** provides the {@link VirtualNetwork} and {@link TravelData} and therefore {@link VirtualNetworkPreparer} has to be run in the Preparer */
public class AmodeusDatabaseModule extends AbstractModule {
    private final MatsimStaticDatabase db;

    public AmodeusDatabaseModule(MatsimStaticDatabase db) {
        this.db = db;
    }

    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public MatsimStaticDatabase provideDatabase() {
        return db;
    }

}
