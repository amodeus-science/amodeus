package org.matsim.amodeus.components;

import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.core.modal.ModalProviders;

public interface AmodeusRouter extends ParallelLeastCostPathCalculator {
    interface Factory {
        AmodeusRouter createRouter(ModalProviders.InstanceGetter inject);
    }
}
