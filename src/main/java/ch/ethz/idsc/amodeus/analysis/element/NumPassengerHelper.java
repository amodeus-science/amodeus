/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;

/* package */ enum NumPassengerHelper {
    ;

    public static boolean isrelevantRequstContainer(RequestContainer rc) {
        return (rc.allStatii().contains(RequestStatus.PICKUP) || rc.allStatii().contains(RequestStatus.DRIVING)) //
                && !rc.allStatii().contains(RequestStatus.DROPOFF);
    }

}
