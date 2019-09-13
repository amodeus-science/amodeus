/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;

/* package */ enum NumPassengerHelper {
    ;

    public static boolean isrelevantRequstContainer(RequestContainer rc) {
        RequestStatus status = rc.getStatus();
        return status.equals(RequestStatus.PICKUP) || status.equals(RequestStatus.DRIVING);
    }

}
