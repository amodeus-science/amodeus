package org.matsim.amodeus.components;

import java.util.List;

import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.core.modal.ModalProviders;

public interface AmodeusGenerator {
    List<DvrpVehicleSpecification> generateVehicles();

    interface AVGeneratorFactory {
        AmodeusGenerator createGenerator(ModalProviders.InstanceGetter inject);
    }
}
