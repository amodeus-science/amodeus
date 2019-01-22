package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Comparator;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum RequestWaitTimeComparator implements Comparator<AVRequest> {
    INSTANCE;

    @Override
    public int compare(AVRequest avr1, AVRequest avr2) {
        /** both requests wait equally long */
        if (avr1.getSubmissionTime() == avr2.getSubmissionTime())
            return 0;

        if (avr1.getSubmissionTime() < avr2.getSubmissionTime()) {
            /** request 1 waits longer than request 2 */
            return -1;
        } else {
            /** request 2 waits longer than request 1 */
            return 1;
        }

    }

}
