/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.util.matsim.SafeConfig;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class GlobalBipartiteMatchingILP extends GlobalBipartiteMatching {

    private final Tensor costFunctionWeights;

    public GlobalBipartiteMatchingILP(GlobalBipartiteCost cost, SafeConfig safeConfig) {
        super(cost);
        /** extract weights for matching algorithm */
        System.out.println("Retrieving weights from the av.xml file, the dispatcher needs a line in this format:");
        System.out.println("<param name=\"matchingWeight\" value=\"{1,2,3}\" />");
        this.costFunctionWeights = Tensors.fromString(safeConfig.getStringStrict("matchingWeight"));
        System.out.println("[alpha,beta,gamma] = " + costFunctionWeights);
    }

    public GlobalBipartiteMatchingILP(GlobalBipartiteCost cost, Tensor customWeights) {
        super(cost);
        this.costFunctionWeights = customWeights;
    }

    @Override
    protected Map<RoboTaxi, PassengerRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<PassengerRequest> requests) {
        return (new GlobalBipartiteHelperILP<PassengerRequest>(new GLPKAssignmentSolverBetter(costFunctionWeights)))//
                .genericMatch(roboTaxis, requests, PassengerRequest::getFromLink, globalBipartiteCost);
    }

    @Override
    protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
        return (new GlobalBipartiteHelperILP<Link>(new GLPKAssignmentSolverBetter(costFunctionWeights)))//
                .genericMatch(roboTaxis, links, l -> l, globalBipartiteCost);
    }
}
