/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import ch.ethz.matsim.av.framework.AVUtils;

public class AmodeusVehicleToVSGeneratorModule extends AbstractModule {
    @Override
    public void install() {
        /** this vehicle generator requires the {@link AmodeusVirtualNetworkModule} to have {@link VirtualNetwork} and {@link TravelData} injected */
        bind(VehicleToVSGenerator.Factory.class);
        AVUtils.bindGeneratorFactory(binder(), VehicleToVSGenerator.class.getSimpleName()).//
                to(VehicleToVSGenerator.Factory.class);
    }
}
