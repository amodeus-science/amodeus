package ch.ethz.idsc.amodeus.matsim.mod;

import java.io.IOException;

import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.mpcsetup.MPCsetup;
import ch.ethz.idsc.amodeus.mpcsetup.MPCsetupGet;

public class AmodeusMPCSetupModule extends AbstractModule {
    @Override
    public void install() {
        // ---
    }

    @Provides
    @Singleton
    public MPCsetup provideMPCsetup() {
        try {
            return MPCsetupGet.buildMPCsetup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }


}
