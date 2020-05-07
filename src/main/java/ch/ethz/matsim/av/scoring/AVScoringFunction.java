package ch.ethz.matsim.av.scoring;

import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.ScoringParameters;

import ch.ethz.matsim.av.financial.PriceCalculator;
import ch.ethz.matsim.av.schedule.AVTransitEvent;

public class AVScoringFunction implements SumScoringFunction.ArbitraryEventScoring {
    final static Logger log = Logger.getLogger(AVScoringFunction.class);

    private final PriceCalculator priceCalculator;

    private AVScoringTrip scoringTrip = null;
    private double score = 0.0;

    private final ScoringParameters standardParameters;
    private final AVScoringParameters avParameters;

    private final List<String> modes;

    public AVScoringFunction(List<String> modes, ScoringParameters standardParameters, AVScoringParameters avParameters, PriceCalculator priceCalculator) {
        this.modes = modes;
        this.standardParameters = standardParameters;
        this.avParameters = avParameters;
        this.priceCalculator = priceCalculator; // TODO: Move(d?) this to routing already!
    }

    private double getMarginalUtilityOfMoney() {
        return standardParameters.marginalUtilityOfMoney;
    }

    private double getMarginalUtilityOfTraveling(String mode) {
        return standardParameters.modeParams.get(mode).marginalUtilityOfTraveling_s;
    }

    private double getMarginalUtilityOfWaiting(String mode) {
        // TODO: Make this mode-dependent!
        return avParameters.marginalUtilityOfWaiting_s;
    }

    private double getStuckUtility() {
        return avParameters.stuckUtility;
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
        if (!Double.isNaN(trip.getPrice())) {
            return -trip.getPrice() * getMarginalUtilityOfMoney();
        } else {
            double price = priceCalculator.calculatePrice(trip.getOperatorId(), trip.getDistance(), trip.getInVehicleTravelTime());
            return -price * getMarginalUtilityOfMoney();
        }
    }

    @Override
    public void finish() {
        if (scoringTrip != null) {
            score += getStuckUtility();
        }
    }

    @Override
    public double getScore() {
        return score;
    }
}
