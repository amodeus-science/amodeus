package ch.ethz.matsim.av.generator;

import java.util.List;

import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.run.ModalProviders;

public interface AVGenerator {
    List<DvrpVehicleSpecification> generateVehicles();

    interface AVGeneratorFactory {
        AVGenerator createGenerator(ModalProviders.InstanceGetter inject);
    }
}
