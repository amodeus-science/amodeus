/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import amodeus.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.Tensor;

@FunctionalInterface
/* package */ interface VirtualNodeFunction {
    Tensor evaluate(SimulationObject ref);
}
