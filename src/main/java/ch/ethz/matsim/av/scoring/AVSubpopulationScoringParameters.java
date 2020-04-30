package ch.ethz.matsim.av.scoring;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.population.PopulationUtils;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.AVScoringParameterSet;

public class AVSubpopulationScoringParameters {
	private final AVConfigGroup config;
	private final String subpopulationAttributeName;

	private final Map<String, AVScoringParameters> cache = new HashMap<>();

	@Inject
	AVSubpopulationScoringParameters(PlansConfigGroup plansConfigGroup, AVConfigGroup config, Population population) {
		this.config = config;
		this.subpopulationAttributeName = plansConfigGroup.getSubpopulationAttributeName();
	}

	public AVScoringParameters getScoringParameters(Person person) {
		final String subpopulation = (String) PopulationUtils.getPersonAttribute(person, subpopulationAttributeName);

		if (!cache.containsKey(subpopulation)) {
			AVScoringParameterSet configParameters = config.getScoringParameters(subpopulation);
			AVScoringParameters simulationParameters = new AVScoringParameters(
					configParameters.getMarginalUtilityOfWaitingTime() / 3600.0, configParameters.getStuckUtility());
			cache.put(subpopulation, simulationParameters);
		}

		return cache.get(subpopulation);
	}
}
