package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;

/* package */ enum NumPassengerHelper {
    ;

    public static Boolean isrelevantRequstContainer(RequestContainer rc) {

        return (rc.requestStatus.contains(RequestStatus.PICKUP) || rc.requestStatus.contains(RequestStatus.DRIVING)) //
                && !rc.requestStatus.contains(RequestStatus.DROPOFF);
    }

}
