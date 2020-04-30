package ch.ethz.matsim.av.scoring;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.core.scoring.SumScoringFunction;

import ch.ethz.matsim.av.financial.PriceCalculator;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.schedule.AVTransitEvent;

public class AVScoringFunction implements SumScoringFunction.ArbitraryEventScoring {
	final static Logger log = Logger.getLogger(AVScoringFunction.class);

	private final PriceCalculator priceCalculator;
	final private double marginalUtilityOfWaiting;
	final private double marginalUtilityOfTraveling;
	final private double marginalUtilityOfMoney;
	final private double stuckUtility;

	private AVScoringTrip scoringTrip = null;
	private double score = 0.0;

	public AVScoringFunction(double marginalUtilityOfMoney, double marginalUtilityOfTraveling,
			double marginalUtilityOfWaiting, double stuckUtility, PriceCalculator priceCalculator) {
		this.marginalUtilityOfWaiting = marginalUtilityOfWaiting;
		this.marginalUtilityOfTraveling = marginalUtilityOfTraveling;
		this.marginalUtilityOfMoney = marginalUtilityOfMoney;
		this.stuckUtility = stuckUtility;
		this.priceCalculator = priceCalculator;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof PersonDepartureEvent) {
			if (((PersonDepartureEvent) event).getLegMode().equals(AVModule.AV_MODE)) {
				if (scoringTrip != null) {
					throw new IllegalStateException();
				}

				scoringTrip = new AVScoringTrip();
				scoringTrip.processDeparture((PersonDepartureEvent) event);
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
		return (marginalUtilityOfWaiting - marginalUtilityOfTraveling) * trip.getWaitingTime();
	}

	static int noPricingWarningCount = 100;

	private double computePricingScoring(AVScoringTrip trip) {
		if (!Double.isNaN(trip.getPrice())) {
			return -trip.getPrice() * marginalUtilityOfMoney;
		} else {
			double price = priceCalculator.calculatePrice(trip.getOperatorId(), trip.getDistance(),
					trip.getInVehicleTravelTime());
			return -price * marginalUtilityOfMoney;
		}
	}

	@Override
	public void finish() {
		if (scoringTrip != null) {
			score += stuckUtility;
		}
	}

	@Override
	public double getScore() {
		return score;
	}
}
