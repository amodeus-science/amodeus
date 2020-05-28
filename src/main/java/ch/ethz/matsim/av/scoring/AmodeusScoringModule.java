package ch.ethz.matsim.av.scoring;

import org.matsim.core.controler.AbstractModule;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.scoring.parameters.AmodeusScoringParametersForPerson;

public class AmodeusScoringModule extends AbstractModule {
    @Override
    public void install() {
        bind(ScoringFunctionFactory.class).to(AmodeusScoringFunctionFactory.class);
        bind(CharyparNagelScoringFunctionFactory.class);
    }

    @Provides
    @Singleton
    public AmodeusScoringFunctionFactory provideScoringFunctionFactory(CharyparNagelScoringFunctionFactory delegate, AmodeusConfigGroup config,
            AmodeusScoringParametersForPerson parameters) {
        return new AmodeusScoringFunctionFactory(delegate, config.getModes().keySet(), parameters);
    }

    @Provides
    @Singleton
    public AmodeusScoringParametersForPerson provideScoringParmametersForPerson(AmodeusConfigGroup amodeusConfig, ScoringParametersForPerson delegate) {
        return new AmodeusScoringParametersForPerson(amodeusConfig, delegate);
    }
}
