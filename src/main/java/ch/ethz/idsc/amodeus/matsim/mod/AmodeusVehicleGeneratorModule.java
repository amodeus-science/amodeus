/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.amodeus.framework.AVUtils;
import org.matsim.core.controler.AbstractModule;

public class AmodeusVehicleGeneratorModule extends AbstractModule {
    @Override
    public void install() {

        /** places vehicles at random sampling according to request density */
        bind(RandomDensityGenerator.Factory.class);
        AVUtils.bindGeneratorFactory(binder(), RandomDensityGenerator.class.getSimpleName()).//
                to(RandomDensityGenerator.Factory.class);

        /** generator used for {@link FeedforwardFluidicTimeVaryingRebalancingPolicy} */
        bind(VehicleToVSGenerator.Factory.class);
        AVUtils.bindGeneratorFactory(binder(), VehicleToVSGenerator.class.getSimpleName()).//
                to(VehicleToVSGenerator.Factory.class);
    }
}
