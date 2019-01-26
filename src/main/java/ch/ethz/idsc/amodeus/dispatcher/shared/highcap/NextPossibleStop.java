/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class NextPossibleStop {
    private final AVRequest avRequest;
    private boolean onboardTrueOrFalse;

    public NextPossibleStop(AVRequest avRequest, boolean onboardTrueOrFalse) {
        this.avRequest = avRequest;
        this.onboardTrueOrFalse = onboardTrueOrFalse;
    }

    public Link getLink() {
        if (onboardTrueOrFalse)
            return avRequest.getToLink();

        return avRequest.getFromLink();
    }

    public boolean getOnboardStatus() {
        return onboardTrueOrFalse;
    }

    public void changeOnboardStatus(boolean isItOnboardNow) {
        onboardTrueOrFalse = isItOnboardNow;
    }

    public AVRequest getAVRequest() {
        return avRequest;
    }
}
