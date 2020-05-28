package org.matsim.amodeus.scoring.parameters;

import java.util.HashMap;
import java.util.Map;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.AmodeusScoringConfig;
import org.matsim.core.scoring.functions.ModeUtilityParameters;
import org.matsim.core.scoring.functions.ScoringParameters;

public class AmodeusModalScoringParameters {
    private final AmodeusConfigGroup config;
    private final ScoringParameters scoringParameters;
    private final String subpopulation;

    private final Map<String, AmodeusScoringParameters> parameters = new HashMap<>();

    AmodeusModalScoringParameters(String subpopulation, AmodeusConfigGroup config, ScoringParameters scoringParameters) {
        this.subpopulation = subpopulation;
        this.config = config;
        this.scoringParameters = scoringParameters;
    }

    public AmodeusScoringParameters get(String mode) {
        if (!parameters.containsKey(mode)) {
            // Fetch Amodeus parameters
            AmodeusModeConfig modeConfig = config.getMode(mode);
            AmodeusScoringConfig modeScoringConfig = modeConfig.getScoringParameters(subpopulation);

            double marginalUtilityOfWaitingTime = modeScoringConfig.getMarginalUtilityOfWaitingTime() / 3600.0;
            double stuckUtility = modeScoringConfig.getStuckUtility();

            // Fetch standard parameters
            ModeUtilityParameters modeParams = scoringParameters.modeParams.get(mode);

            if (modeParams == null) {
                throw new IllegalStateException(String.format("No scoring parameters defined for mode '%s' in planCalcScore.", mode));
            }

            double marginalUtilityOfTravelTime = modeParams.marginalUtilityOfTraveling_s;
            boolean hasMonetaryDistanceRate = modeParams.monetaryDistanceCostRate != 0.0;

            double marginalUtilityOfMoney = scoringParameters.marginalUtilityOfMoney;

            parameters.put(mode,
                    new AmodeusScoringParameters(marginalUtilityOfTravelTime, marginalUtilityOfWaitingTime, stuckUtility, marginalUtilityOfMoney, hasMonetaryDistanceRate));
        }

        return parameters.get(mode);
    }
}
