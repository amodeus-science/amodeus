package org.matsim.amodeus.scoring;

import java.util.Collection;

import org.matsim.amodeus.dvrp.request.AmodeusRequestEvent;
import org.matsim.amodeus.scoring.parameters.AmodeusModalScoringParameters;
import org.matsim.amodeus.scoring.parameters.AmodeusScoringParameters;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.core.scoring.SumScoringFunction;

public class AmodeusScoringFunction implements SumScoringFunction.ArbitraryEventScoring {
    private final Collection<String> modes;
    private final AmodeusModalScoringParameters modalParameters;

    private boolean ongoing = false;
    private double departureTime = Double.NaN;
    private AmodeusScoringParameters parameters = null;

    private double score = 0.0;

    public AmodeusScoringFunction(Collection<String> modes, AmodeusModalScoringParameters modalParameters) {
        this.modes = modes;
        this.modalParameters = modalParameters;
    }

    @Override
    public double getScore() {
        return this.score;
    }

    private void processDepartureEvent(PersonDepartureEvent event) {
        if (modes.contains(event.getLegMode())) {
            this.departureTime = event.getTime();
            this.parameters = modalParameters.get(event.getLegMode());
            this.ongoing = true;
        }
    }

    private void processRequestEvent(AmodeusRequestEvent event) {
        if (event.getExpectedPrice().isPresent()) {
            if (parameters.hasMonetaryDistanceRate) {
                throw new IllegalStateException("Non-zero monetary distance rate is given, while Amodeus is performing price predictions "
                        + "in the routing. In this case, the scoring considers the predicted price using the marginalUtilityOfMoney. "
                        + "Defining a monetaryDistanceRate at the same time would confuse the result of the scoring!");
            }

            this.score -= parameters.marginalUtilityOfMoney * event.getExpectedPrice().get();
        }
    }

    private void processEnterEvent(PersonEntersVehicleEvent event) {
        double waitingTime = event.getTime() - departureTime;

        this.score -= parameters.marginalUtilityOfTravelTime * waitingTime; // To compensate for standard leg scoring
        this.score += parameters.marginalUtilityOfWaitingTime * waitingTime; // To consider waiting time properly
    }

    @Override
    public void handleEvent(Event event) {
        if (event instanceof PersonDepartureEvent) {
            processDepartureEvent((PersonDepartureEvent) event);
        } else if (ongoing && event instanceof AmodeusRequestEvent) {
            processRequestEvent((AmodeusRequestEvent) event);
        } else if (ongoing && event instanceof PersonEntersVehicleEvent) {
            processEnterEvent((PersonEntersVehicleEvent) event);
        } else if (ongoing && event instanceof PersonArrivalEvent) {
            ongoing = false;
        }
    }

    @Override
    public void finish() {
        if (ongoing) {
            // Agent never entered the vehicle or is still en route
            score += parameters.stuckUtility;
        }
    }
}
