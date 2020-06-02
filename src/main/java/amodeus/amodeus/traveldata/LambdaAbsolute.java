/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.traveldata;

import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.prep.PopulationAVRequests;
import amodeus.amodeus.prep.PopulationArrivalRate;
import amodeus.amodeus.prep.Request;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum LambdaAbsolute {
    ;

    /** returns the lambdaAbsolute {@link Tensor} that represents all requests in the population.
     * E.g. lambdaAbsolute(k,i,j)=n means that n requests appear at timeInterval k
     * with departure in virtual node i and destination in virtual node j */
    public static Tensor get(Network network, VirtualNetwork<Link> virtualNetwork, //
            Population population, int timeIntervalLength, int endTime) {
        Set<Request> avRequests = PopulationAVRequests.get(population, network, endTime);
        return PopulationArrivalRate.getVNodeAndInterval(avRequests, virtualNetwork, timeIntervalLength, endTime);
    }

}
