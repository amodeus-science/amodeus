package org.matsim.amodeus.components;

import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.contrib.dvrp.run.ModalProviders;

public interface AVRouter extends ParallelLeastCostPathCalculator {
    interface Factory {
        AVRouter createRouter(ModalProviders.InstanceGetter inject);
    }
}
