package ch.ethz.matsim.av.generator;

import java.util.List;

import org.matsim.contrib.dvrp.run.ModalProviders;

import ch.ethz.matsim.av.data.AVVehicle;

public interface AVGenerator {
    List<AVVehicle> generateVehicles();

    interface AVGeneratorFactory {
        AVGenerator createGenerator(ModalProviders.InstanceGetter inject);
    }
}
