package org.matsim.amodeus.scoring.parameters;

import java.util.HashMap;
import java.util.Map;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

public class AmodeusScoringParametersForPerson {
    private final AmodeusConfigGroup amodeusConfig;
    private final ScoringParametersForPerson delegate;
    private final Map<String, AmodeusModalScoringParameters> parameters = new HashMap<>();

    public AmodeusScoringParametersForPerson(AmodeusConfigGroup amodeusConfig, ScoringParametersForPerson delegate) {
        this.amodeusConfig = amodeusConfig;
        this.delegate = delegate;
    }

    public AmodeusModalScoringParameters getScoringParameters(Person person) {
        String subpopulation = PopulationUtils.getSubpopulation(person);
        ScoringParameters scoringParameters = delegate.getScoringParameters(person);

        if (!parameters.containsKey(subpopulation)) {
            parameters.put(subpopulation, new AmodeusModalScoringParameters(subpopulation, amodeusConfig, scoringParameters));
        }

        return parameters.get(subpopulation);
    }
}
