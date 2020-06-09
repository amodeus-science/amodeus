/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import amodeus.amodeus.dispatcher.core.RequestStatus;
import amodeus.amodeus.net.RequestContainer;

/* package */ enum NumPassengerHelper {
    ;

    public static boolean isrelevantRequstContainer(RequestContainer rc) {
        return (rc.requestStatus.contains(RequestStatus.PICKUP) || rc.requestStatus.contains(RequestStatus.DRIVING)) //
                && !rc.requestStatus.contains(RequestStatus.DROPOFF);
    }

}
