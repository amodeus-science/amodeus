/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.amodeus.framework.AVUtils;
import org.matsim.core.controler.AbstractModule;

public class AmodeusVehicleToVSGeneratorModule extends AbstractModule {
    @Override
    public void install() {
        /** this vehicle generator requires the {@link AmodeusVirtualNetworkModule} to have {@link VirtualNetwork} and {@link TravelData} injected */
        bind(VehicleToVSGenerator.Factory.class);
        AVUtils.bindGeneratorFactory(binder(), VehicleToVSGenerator.class.getSimpleName()).//
                to(VehicleToVSGenerator.Factory.class);
    }
}
