package ch.ethz.matsim.av.scoring;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.financial.PriceCalculator;
import ch.ethz.matsim.av.framework.AVModule;

@Singleton
public class AVScoringFunctionFactory implements ScoringFunctionFactory {
	final private ScoringFunctionFactory delegate;
	final private ScoringParametersForPerson defaultParameters;
	final private AVSubpopulationScoringParameters avParameters;
	private final PriceCalculator priceCalculator;

	@Inject
	public AVScoringFunctionFactory(Scenario scenario, ScoringParametersForPerson defaultParameters,
			AVSubpopulationScoringParameters avParameters, PriceCalculator priceCalculator) {
		this.defaultParameters = defaultParameters;
		this.avParameters = avParameters;
		this.priceCalculator = priceCalculator;

		delegate = new CharyparNagelScoringFunctionFactory(scenario);
	}

	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		SumScoringFunction sf = (SumScoringFunction) delegate.createNewScoringFunction(person);

		ScoringParameters personDefaultParameters = defaultParameters.getScoringParameters(person);
		AVScoringParameters personAvParameters = avParameters.getScoringParameters(person);

		double marginalUtilityOfMoney = personDefaultParameters.marginalUtilityOfMoney;
		double marginalUtilityOfTraveling = personDefaultParameters.modeParams
				.get(AVModule.AV_MODE).marginalUtilityOfTraveling_s;
		double marginalUtilityOfWaiting = personAvParameters.marginalUtilityOfWaiting_s;
		double stuckUtility = personAvParameters.stuckUtility;

		sf.addScoringFunction(new AVScoringFunction(marginalUtilityOfMoney, marginalUtilityOfTraveling,
				marginalUtilityOfWaiting, stuckUtility, priceCalculator));

		return sf;
	}
}
