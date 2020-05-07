package ch.ethz.matsim.av.router;

import org.matsim.contrib.dvrp.run.ModalProviders;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public interface AVRouter extends ParallelLeastCostPathCalculator {
    interface Factory {
        AVRouter createRouter(ModalProviders.InstanceGetter inject);
    }
}
