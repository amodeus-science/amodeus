package org.matsim.amodeus.components;

import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.contrib.dvrp.run.ModalProviders;

public interface AmodeusRouter extends ParallelLeastCostPathCalculator {
    interface Factory {
        AmodeusRouter createRouter(ModalProviders.InstanceGetter inject);
    }
}
