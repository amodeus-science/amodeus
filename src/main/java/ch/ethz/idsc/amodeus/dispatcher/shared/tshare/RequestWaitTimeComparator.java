/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Comparator;

import ch.ethz.matsim.av.passenger.AVRequest;

/** This {@link Comparator} is used to sort {@link AVRequest}s with ascending wait time
 * which is the same as ascending submission time */
/* package */ enum RequestWaitTimeComparator implements Comparator<AVRequest> {
    INSTANCE;

    @Override
    public int compare(AVRequest request1, AVRequest request2) {
        return Double.compare(request1.getSubmissionTime(), request2.getSubmissionTime());
    }
}
