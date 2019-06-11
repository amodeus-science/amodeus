/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import javax.sound.midi.SysexMessage;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public class GlobalBipartiteMatchingILP extends GlobalBipartiteMatching {

	private final Tensor costFunctionWeights;

	public GlobalBipartiteMatchingILP(DistanceFunction distanceFunction, SafeConfig safeConfig) {
		super(distanceFunction);
		/** extract weights for matching algorithm */
		System.out.println("Retrieving weights from the av.xml file, the dispatcher needs a line in this format:");
		System.out.println("<param name=\"matchingWeight\" value=\"{1,2,3}\" />");
		this.costFunctionWeights = Tensors.fromString(safeConfig.getStringStrict("matchingWeight"));
		System.out.println("[alpha,beta,gamma] = " + costFunctionWeights);
	}

	public GlobalBipartiteMatchingILP(DistanceFunction distanceFunction, Tensor customWeights) {
		super(distanceFunction);
		this.costFunctionWeights = customWeights;
	}

	@Override
	protected Map<RoboTaxi, AVRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
		return (new GlobalBipartiteHelperILP<AVRequest>(new GLPKAssignmentSolverBetter(costFunctionWeights)))//
				.genericMatch(roboTaxis, requests, AVRequest::getFromLink, specificWeight);
	}

	@Override
	protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
		return (new GlobalBipartiteHelperILP<Link>(new GLPKAssignmentSolverBetter(costFunctionWeights)))//
				.genericMatch(roboTaxis, links, l -> l, specificWeight);
	}
}
