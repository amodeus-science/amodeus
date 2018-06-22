/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.IOException;

import org.matsim.api.core.v01.population.Population;

public interface PopulationCutterFunction {
	void process(Population population) throws IOException, Exception;

	void printCutSummary();
}
