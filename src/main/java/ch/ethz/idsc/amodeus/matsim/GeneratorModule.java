/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

import org.matsim.amodeus.framework.AmodeusUtils;
import org.matsim.core.controler.AbstractModule;

import ch.ethz.idsc.amodeus.generator.RandomDensityGenerator;
import ch.ethz.idsc.amodeus.generator.VehicleToVSGenerator;

public class GeneratorModule extends AbstractModule {
    @Override
    public void install() {

        /** places vehicles at random sampling according to request density */
        bind(RandomDensityGenerator.Factory.class);
        AmodeusUtils.bindGeneratorFactory(binder(), RandomDensityGenerator.class.getSimpleName()).//
                to(RandomDensityGenerator.Factory.class);

        /** generator used for {@link FeedforwardFluidicTimeVaryingRebalancingPolicy} */
        bind(VehicleToVSGenerator.Factory.class);
        AmodeusUtils.bindGeneratorFactory(binder(), VehicleToVSGenerator.class.getSimpleName()).//
                to(VehicleToVSGenerator.Factory.class);
    }
}
