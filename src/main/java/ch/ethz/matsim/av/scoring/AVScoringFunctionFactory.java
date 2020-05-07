package ch.ethz.matsim.av.scoring;

import java.util.List;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

import ch.ethz.matsim.av.financial.PriceCalculator;

public class AVScoringFunctionFactory implements ScoringFunctionFactory {
    final private ScoringFunctionFactory delegate;
    final private ScoringParametersForPerson defaultParameters;
    final private AVSubpopulationScoringParameters avParameters;
    private final PriceCalculator priceCalculator;
    private final List<String> modes;

    public AVScoringFunctionFactory(Scenario scenario, ScoringParametersForPerson defaultParameters, AVSubpopulationScoringParameters avParameters, PriceCalculator priceCalculator,
            List<String> modes) {
        this.defaultParameters = defaultParameters;
        this.avParameters = avParameters;
        this.priceCalculator = priceCalculator;
        this.modes = modes;

        delegate = new CharyparNagelScoringFunctionFactory(scenario);
    }

    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        SumScoringFunction sf = (SumScoringFunction) delegate.createNewScoringFunction(person);

        ScoringParameters personDefaultParameters = defaultParameters.getScoringParameters(person);
        AVScoringParameters personAvParameters = avParameters.getScoringParameters(person);

        sf.addScoringFunction(new AVScoringFunction(modes, personDefaultParameters, personAvParameters, priceCalculator));

        return sf;
    }
}
