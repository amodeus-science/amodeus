package org.matsim.amodeus.scoring;

import java.util.Collection;

import org.matsim.amodeus.scoring.parameters.AmodeusScoringParametersForPerson;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;

public class AmodeusScoringFunctionFactory implements ScoringFunctionFactory {
    private final ScoringFunctionFactory delegate;
    private final AmodeusScoringParametersForPerson parameters;
    private final Collection<String> modes;

    public AmodeusScoringFunctionFactory(ScoringFunctionFactory delegate, Collection<String> modes, AmodeusScoringParametersForPerson parameters) {
        this.delegate = delegate;
        this.modes = modes;
        this.parameters = parameters;
    }

    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        SumScoringFunction scoringFunction = (SumScoringFunction) delegate.createNewScoringFunction(person);
        scoringFunction.addScoringFunction(new AmodeusScoringFunction(modes, parameters.getScoringParameters(person)));
        return scoringFunction;
    }
}
