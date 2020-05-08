package ch.ethz.matsim.av.scoring;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.ScoringParameters;

import ch.ethz.matsim.av.schedule.AVTransitEvent;

public class AVScoringFunction implements SumScoringFunction.ArbitraryEventScoring {
    final static Logger log = Logger.getLogger(AVScoringFunction.class);

    private AVScoringTrip scoringTrip = null;
    private double score = 0.0;

    private final ScoringParameters standardParameters;
    private final Map<String, AVScoringParameters> avParameters;

    private final Collection<String> modes;

    public AVScoringFunction(Collection<String> modes, ScoringParameters standardParameters, Map<String, AVScoringParameters> avParameters) {
        this.modes = modes;
        this.standardParameters = standardParameters;
        this.avParameters = avParameters;
    }

    private double getMarginalUtilityOfMoney() {
        return standardParameters.marginalUtilityOfMoney;
    }

    private double getMarginalUtilityOfTraveling(String mode) {
        return standardParameters.modeParams.get(mode).marginalUtilityOfTraveling_s;
    }

    private AVScoringParameters getModeParameters(String mode) {
        AVScoringParameters parameters = avParameters.get(mode);

        if (parameters != null) {
            return parameters;
        }

        throw new IllegalStateException("No scoring parameters in AMoDeus defined for mode: " + mode);
    }

    private double getMarginalUtilityOfWaiting(String mode) {
        return getModeParameters(mode).marginalUtilityOfWaiting_s;
    }

    private double getStuckUtility(String mode) {
        return getModeParameters(mode).stuckUtility;
    }

    @Override
    public void handleEvent(Event event) {
        // TODO: Validate here that we do not switch modes in between

        if (event instanceof PersonDepartureEvent) {
            String mode = ((PersonDepartureEvent) event).getLegMode();

            if (modes.contains(mode)) {
                if (scoringTrip != null) {
                    throw new IllegalStateException();
                }

                scoringTrip = new AVScoringTrip();
                scoringTrip.processDeparture(mode, (PersonDepartureEvent) event);
            }
        } else if (event instanceof PersonEntersVehicleEvent) {
            if (scoringTrip != null) {
                scoringTrip.processEnterVehicle((PersonEntersVehicleEvent) event);
            }
        } else if (event instanceof AVTransitEvent) {
            if (scoringTrip != null) {
                scoringTrip.processTransit((AVTransitEvent) event);
            }
        }

        if (scoringTrip != null && scoringTrip.isFinished()) {
            handleScoringTrip(scoringTrip);
            scoringTrip = null;
        }
    }

    private void handleScoringTrip(AVScoringTrip trip) {
        score += computeWaitingTimeScoring(trip);
        score += computePricingScoring(trip);
    }

    private double computeWaitingTimeScoring(AVScoringTrip trip) {
        // Compensate for the travel disutility
        return (getMarginalUtilityOfWaiting(trip.getMode()) - getMarginalUtilityOfTraveling(trip.getMode())) * trip.getWaitingTime();
    }

    static int noPricingWarningCount = 100;

    private double computePricingScoring(AVScoringTrip trip) {
        return -trip.getPrice() * getMarginalUtilityOfMoney();
    }

    @Override
    public void finish() {
        if (scoringTrip != null) {
            score += getStuckUtility(scoringTrip.getMode());
        }
    }

    @Override
    public double getScore() {
        return score;
    }
}
