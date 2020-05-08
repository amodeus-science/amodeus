package ch.ethz.matsim.av.scoring;

import java.util.Collection;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

public class AVScoringFunctionFactory implements ScoringFunctionFactory {
    final private ScoringFunctionFactory delegate;
    final private ScoringParametersForPerson defaultParameters;
    final private AVSubpopulationScoringParameters avParameters;
    private final Collection<String> modes;

    public AVScoringFunctionFactory(Scenario scenario, ScoringParametersForPerson defaultParameters, AVSubpopulationScoringParameters avParameters, Collection<String> modes) {
        this.defaultParameters = defaultParameters;
        this.avParameters = avParameters;
        this.modes = modes;

        delegate = new CharyparNagelScoringFunctionFactory(scenario);
    }

    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        SumScoringFunction sf = (SumScoringFunction) delegate.createNewScoringFunction(person);

        ScoringParameters personDefaultParameters = defaultParameters.getScoringParameters(person);

        sf.addScoringFunction(new AVScoringFunction(modes, personDefaultParameters, avParameters.getScoringParameters(person)));

        return sf;
    }
}
