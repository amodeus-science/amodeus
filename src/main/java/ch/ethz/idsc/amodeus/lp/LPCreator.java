/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.LPOptions;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum LPCreator {
    NONE {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, LPOptions lpOptions, Tensor lambdaAbsolute, int numberOfVehicles, int endTime) {
            return new LPEmpty(virtualNetwork, lambdaAbsolute, endTime);
        }
    },
    TIMEINVARIANT {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, LPOptions lpOptions, Tensor lambdaAbsolute, int numberOfVehicles, int endTime) {
            return new LPTimeInvariant(virtualNetwork, lambdaAbsolute, numberOfVehicles, endTime);
        }
    },
    TIMEVARIANT {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, LPOptions lpOptions, Tensor lambdaAbsolute, int numberOfVehicles, int endTime) {
            return new LPTimeVariant(virtualNetwork, network, lpOptions, lambdaAbsolute, numberOfVehicles, endTime);
        }
    };

    public abstract LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, LPOptions lpOptions, Tensor lambdaAbsolute, int numberOfVehicles, int endTime);
}
