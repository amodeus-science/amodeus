package ch.ethz.matsim.av.scoring;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;

import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.config.modal.AmodeusScoringConfig;

public class AVSubpopulationScoringParameters {
    private final Map<String, Map<String, AVScoringParameters>> cache = new HashMap<>();

    @Inject
    AVSubpopulationScoringParameters(AmodeusConfigGroup config) {
        for (AmodeusModeConfig modeConfig : config.getModes().values()) {
            for (AmodeusScoringConfig set : modeConfig.getScoringParameters()) {
                Map<String, AVScoringParameters> subpopulationParameters = cache.get(set.getSubpopulation());

                if (subpopulationParameters == null) {
                    subpopulationParameters = new HashMap<>();
                    cache.put(set.getSubpopulation(), subpopulationParameters);
                }

                subpopulationParameters.put(modeConfig.getMode(), AVScoringParameters.fromParameterSet(set));
            }
        }
    }

    public Map<String, AVScoringParameters> getScoringParameters(Person person) {
        String subpopulation = PopulationUtils.getSubpopulation(person);
        Map<String, AVScoringParameters> subpopulationParameters = cache.get(subpopulation);

        if (subpopulationParameters != null) {
            return subpopulationParameters;
        }

        throw new IllegalStateException(String.format("No scoring parameters defined in AMoDeus for subpopulation %s", subpopulation));
    }
}
