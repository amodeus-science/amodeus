/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.core.controler.AbstractModule;

import ch.ethz.matsim.av.framework.AVUtils;

public class AmodeusVehicleGeneratorModule extends AbstractModule {
    @Override
    public void install() {
        bind(RandomDensityGenerator.Factory.class);
        AVUtils.bindGeneratorFactory(binder(), RandomDensityGenerator.class.getSimpleName()).//
                to(RandomDensityGenerator.Factory.class);
        bind(VehicleToVSGenerator.Factory.class);

        /** this vehicle generator requires the {@link AmodeusVirtualNetworkModule} to have {@link VirtualNetwork} and {@link TravelData} injected */
        AVUtils.bindGeneratorFactory(binder(), VehicleToVSGenerator.class.getSimpleName()).//
                to(VehicleToVSGenerator.Factory.class);
    }
}
