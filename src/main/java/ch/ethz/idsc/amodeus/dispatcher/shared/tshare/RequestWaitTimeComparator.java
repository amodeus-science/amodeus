/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Comparator;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum RequestWaitTimeComparator implements Comparator<AVRequest> {
    INSTANCE;

    @Override
    public int compare(AVRequest avr1, AVRequest avr2) {
        return Double.compare(avr1.getSubmissionTime(), avr2.getSubmissionTime());
    }
}
