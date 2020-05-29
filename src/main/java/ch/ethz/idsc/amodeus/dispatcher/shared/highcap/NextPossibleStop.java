/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

/* package */ class NextPossibleStop {
    private final PassengerRequest avRequest;
    private boolean onboardTrueOrFalse;

    public NextPossibleStop(PassengerRequest avRequest, boolean onboardTrueOrFalse) {
        this.avRequest = avRequest;
        this.onboardTrueOrFalse = onboardTrueOrFalse;
    }

    public Link getLink() {
        return onboardTrueOrFalse ? avRequest.getToLink() : avRequest.getFromLink();
    }

    public boolean getOnboardStatus() {
        return onboardTrueOrFalse;
    }

    public void changeOnboardStatus(boolean isItOnboardNow) {
        onboardTrueOrFalse = isItOnboardNow;
    }

    public PassengerRequest getPassengerRequest() {
        return avRequest;
    }
}
