/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Comparator;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

/** This {@link Comparator} is used to sort {@link PassengerRequest}s with ascending wait time
 * which is the same as ascending submission time */
/* package */ enum RequestWaitTimeComparator implements Comparator<PassengerRequest> {
    INSTANCE;

    @Override
    public int compare(PassengerRequest request1, PassengerRequest request2) {
        return Double.compare(request1.getSubmissionTime(), request2.getSubmissionTime());
    }
}
