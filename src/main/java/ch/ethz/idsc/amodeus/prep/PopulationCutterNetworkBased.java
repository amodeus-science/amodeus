/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

public class PopulationCutterNetworkBased implements PopulationCutterFunction {

	private final Network network;

	public PopulationCutterNetworkBased(Network network) {
		this.network = network;
	}

	@Override
	public void process(Population population) {
		PopulationTools.removeOutsideNetwork(population, network);
	}

	@Override
	public void printCutSummary() {
		System.out.println("Population cutter: network based.");
		System.out.println("Population outside network removed.");
	}

}
