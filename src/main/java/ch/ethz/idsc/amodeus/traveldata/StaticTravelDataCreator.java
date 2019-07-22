/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.lp.LPPreparer;
import ch.ethz.idsc.amodeus.lp.LPSolver;
import ch.ethz.idsc.amodeus.prep.PopulationAVRequests;
import ch.ethz.idsc.amodeus.prep.PopulationArrivalRate;
import ch.ethz.idsc.amodeus.prep.Request;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum StaticTravelDataCreator {
    ;
    /** Creates the travel data by counting all travel requests and solving an LP depending on this request information */
    public static StaticTravelData create(File workingDirectory, VirtualNetwork<Link> virtualNetwork, Network network, Population population, int interval, int numberOfVehicles,
            int endTime) throws Exception {
        Tensor lambdaAbsolute = getLambdaAbsolute(network, virtualNetwork, population, interval, endTime);

        LPSolver lpSolver = LPPreparer.run(workingDirectory, virtualNetwork, network, lambdaAbsolute, numberOfVehicles, endTime);

        String lpName = lpSolver.getClass().getSimpleName();
        Tensor alphaAbsolute = lpSolver.getAlphaAbsolute_ij();
        Tensor v0_i = lpSolver.getV0_i();
        Tensor fAbsolute = lpSolver.getFAbsolute_ij();

        return new StaticTravelData(virtualNetwork.getvNetworkID(), lambdaAbsolute, alphaAbsolute, fAbsolute, v0_i, lpName, endTime);
    }

    /** returns the lambdaAbsolute {@link Tensor} that represents all requests in the population.
     * E.g. lambdaAbsolute(k,i,j)=n means that n requests appear at timeInterval k
     * with departure in virtual node i and destination in virtual node j */
    public static Tensor getLambdaAbsolute(Network network, VirtualNetwork<Link> virtualNetwork, Population population, int timeIntervalLength, int endTime) {
        Set<Request> avRequests = PopulationAVRequests.get(population, network, endTime);
        return PopulationArrivalRate.getVNodeAndInterval(avRequests, virtualNetwork, timeIntervalLength, endTime);
    }
}